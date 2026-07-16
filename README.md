# Document Platform

A study project for building an event-driven document upload and processing platform with Java, Spring Boot, Apache Kafka, PostgreSQL, Flyway, Docker, Domain-Driven Design (DDD), Clean Architecture principles, and Magalu Cloud Object Storage.

The project is intentionally developed in small phases. Each phase introduces one new architectural concept only when the project needs it.

---

## Current Status

The project currently contains two independent microservices:

```text
document-platform
├── document-api
└── document-processor
```

### `document-api`

Responsible for:

- receiving document uploads through HTTP;
- uploading original files to Magalu Cloud Object Storage;
- saving document metadata in PostgreSQL;
- publishing `DocumentUploaded` events to Kafka;
- consuming `DocumentProcessed` events from Kafka;
- updating document status in PostgreSQL.

### `document-processor`

Responsible for:

- consuming `DocumentUploaded` events from Kafka;
- downloading uploaded files from Magalu Cloud Object Storage;
- processing `.txt` files;
- generating text processing statistics;
- publishing `DocumentProcessed` events to Kafka.

The processor does **not** access PostgreSQL and does **not** call `document-api` through HTTP.

---

## Current Architecture

```text
Client
  |
  | POST /documents/upload
  v
+------------------------------+
| document-api                 |
|                              |
| - receives multipart file    |
| - uploads to Object Storage  |
| - saves metadata             |
| - publishes DocumentUploaded |
+--------------+---------------+
               |
               | document.uploaded.v1
               v
            +-------+
            | Kafka |
            +---+---+
                |
                v
+-------------------------------+
| document-processor            |
|                               |
| - consumes DocumentUploaded   |
| - downloads file by storageKey|
| - processes text              |
| - publishes DocumentProcessed |
+---------------+---------------+
                |
                | document.processed.v1
                v
            +-------+
            | Kafka |
            +---+---+
                |
                v
+-------------------------------+
| document-api                  |
|                               |
| - consumes DocumentProcessed  |
| - updates PostgreSQL          |
| - marks document as PROCESSED |
+-------------------------------+
```

---

## Implemented Flow

The intended asynchronous flow is now:

```text
document-api
   |
   | DocumentUploaded
   v
Kafka
   |
   v
document-processor
   |
   | DocumentProcessed
   v
Kafka
   |
   v
document-api
   |
   v
PostgreSQL
```

This allows the services to remain independent:

```text
document-api owns PostgreSQL
document-processor owns processing
Kafka connects both services asynchronously
Object Storage stores the original files
```

---

## Technologies

```text
Java 25
Spring Boot 4.1
Spring Web MVC
Spring Data JPA
Spring Kafka
Apache Kafka 4.1
PostgreSQL 16
Flyway
AWS SDK for Java 2.x
Magalu Cloud Object Storage
Docker
Docker Compose
Gradle 9
Jackson
```

---

## Docker Compose Services

The local environment runs:

```text
Docker Compose
├── document-api
├── document-processor
├── document-kafka
└── document-api-postgres
```

`document-api` exposes:

```text
http://localhost:8080
```

Kafka is reachable inside Docker as:

```text
kafka:9092
```

PostgreSQL is reachable inside Docker as:

```text
postgres:5432
```

---

## Event Topics

### `document.uploaded.v1`

Produced by:

```text
document-api
```

Consumed by:

```text
document-processor
```

Used when a document has been uploaded and is ready to be processed.

### `document.processed.v1`

Produced by:

```text
document-processor
```

Consumed by:

```text
document-api
```

Used when the processor has completed text processing for a document.

---

## `DocumentUploaded` Event

Example:

```json
{
  "eventId": "271e0a85-1b8b-4ac4-8597-1568eb7cee2f",
  "documentId": "8f6e110d-8083-4e50-88d7-3942c695d590",
  "storageKey": "documents/2026/07/16/example-file.txt",
  "contentType": "text/plain",
  "size": 60
}
```

Fields:

| Field | Description |
|---|---|
| `eventId` | Unique identifier of the event |
| `documentId` | Identifier of the uploaded document |
| `storageKey` | Object Storage key used to locate the file |
| `contentType` | MIME type of the uploaded file |
| `size` | Original file size in bytes |

Kafka message key:

```text
documentId
```

---

## `DocumentProcessed` Event

Example:

```json
{
  "eventId": "ce29e55a-bfe6-46d4-867c-2dac17cee35b",
  "documentId": "389141ed-35d5-4d76-8062-5de14ab88dfa",
  "characterCount": 60,
  "wordCount": 9,
  "lineCount": 2,
  "processedAt": "2026-07-16T09:30:25.194Z"
}
```

Fields:

| Field | Description |
|---|---|
| `eventId` | Unique identifier of the processed event |
| `documentId` | Identifier of the processed document |
| `characterCount` | Number of characters in the text |
| `wordCount` | Number of words in the text |
| `lineCount` | Number of lines in the text |
| `processedAt` | Timestamp when processing completed |

Kafka message key:

```text
documentId
```

---

## Why the Events Use `storageKey`

Kafka events do not carry file bytes. Instead, the upload event carries the `storageKey`.

The file itself is stored in Object Storage.

```text
Good:
DocumentUploaded { documentId, storageKey }

Bad:
DocumentUploaded { documentId, full file bytes }
```

The processor uses the `storageKey` to download the original file only when needed.

---

## Service Boundaries

### `document-api`

Owns:

```text
documents table
document metadata
document status
HTTP upload endpoint
DocumentUploaded publishing
DocumentProcessed consumption
```

Does not own:

```text
text processing logic
```

### `document-processor`

Owns:

```text
document processing logic
DocumentUploaded consumption
DocumentProcessed publishing
```

Does not own:

```text
documents table
PostgreSQL
HTTP upload endpoint
```

This separation keeps both services independently deployable.

---

## `document-api` Structure

Simplified structure:

```text
document-api
└── src/main/java/io/lossantis/documentapi
    ├── DocumentApiApplication.java
    ├── config
    │   └── JacksonConfig.java
    └── document
        ├── application
        │   ├── processing
        │   │   ├── MarkDocumentAsProcessedCommand.java
        │   │   └── MarkDocumentAsProcessedUseCase.java
        │   └── upload
        │       └── ...
        ├── domain
        │   ├── model
        │   │   ├── Document.java
        │   │   └── DocumentStatus.java
        │   └── repository
        │       └── DocumentRepository.java
        ├── infrastructure
        │   ├── messaging
        │   │   ├── DocumentUploadedEvent.java
        │   │   ├── KafkaDocumentUploadedPublisher.java
        │   │   ├── DocumentProcessedEvent.java
        │   │   └── DocumentProcessedKafkaConsumer.java
        │   ├── persistence
        │   │   ├── DocumentJpaEntity.java
        │   │   ├── JpaDocumentRepositoryAdapter.java
        │   │   └── SpringDataDocumentRepository.java
        │   └── storage
        │       ├── MagaluDocumentStorage.java
        │       ├── MagaluObjectStorageConfig.java
        │       └── MagaluObjectStorageProperties.java
        └── presentation
            └── DocumentController.java
```

The important direction is:

```text
presentation -> application -> domain
infrastructure -> application/domain
```

---

## `document-processor` Structure

Simplified structure:

```text
document-processor
└── src/main/java/io/lossantis/documentprocessor
    ├── DocumentProcessorApplication.java
    ├── config
    │   └── JacksonConfig.java
    └── document
        ├── application
        │   ├── ProcessDocumentCommand.java
        │   ├── ProcessDocumentUseCase.java
        │   ├── messaging
        │   │   └── DocumentProcessedPublisher.java
        │   └── storage
        │       └── DocumentStorage.java
        ├── domain
        │   ├── ProcessedText.java
        │   └── TextDocumentProcessor.java
        └── infrastructure
            ├── messaging
            │   ├── DocumentUploadedEvent.java
            │   ├── DocumentUploadedKafkaConsumer.java
            │   ├── DocumentProcessedEvent.java
            │   └── KafkaDocumentProcessedPublisher.java
            └── storage
                ├── MagaluDocumentStorage.java
                ├── MagaluObjectStorageConfig.java
                └── MagaluObjectStorageProperties.java
```

The processor has two Kafka adapters:

```text
input:  DocumentUploadedKafkaConsumer
output: KafkaDocumentProcessedPublisher
```

---

## Domain Processing

The processor currently handles `.txt` files.

`TextDocumentProcessor` receives a text string and returns `ProcessedText`.

`ProcessedText` contains:

```text
characterCount
wordCount
lineCount
```

Example input:

```text
Kafka makes services independent
This document has two lines
```

Expected result:

```text
characterCount: 60
wordCount: 9
lineCount: 2
```

---

## Object Storage

The project uses Magalu Cloud Object Storage through its S3-compatible API.

Both services use the AWS SDK for Java 2.x.

### `document-api`

Uses Object Storage to upload the original file.

### `document-processor`

Uses Object Storage to download the original file by `storageKey`.

### Storage Key Format

Example:

```text
documents/2026/07/16/af8757ad-bfe4-4506-8181-63388a8dd04c-processed-event-test.txt
```

General format:

```text
documents/YYYY/MM/DD/UUID-original-filename
```

---

## Environment Variables

Create a `.env` file at the project root.

Example:

```dotenv
DOCUMENT_API_DATASOURCE_URL=jdbc:postgresql://postgres:5432/documentdb
DOCUMENT_API_DATASOURCE_USERNAME=postgres
DOCUMENT_API_DATASOURCE_PASSWORD=postgres

MAGALU_OBJECT_STORAGE_ENDPOINT=https://br-se1.magaluobjects.com
MAGALU_OBJECT_STORAGE_REGION=br-se1
MAGALU_OBJECT_STORAGE_BUCKET=document-platform
MAGALU_OBJECT_STORAGE_ACCESS_KEY=your-access-key
MAGALU_OBJECT_STORAGE_SECRET_KEY=your-secret-key
```

The `.env` file must not be committed.

Keep only:

```text
.env.example
```

in the repository.

---

## Jackson and Java Time

The project uses `Instant` in events such as:

```text
processedAt
```

Jackson requires the Java Time module to serialize and deserialize Java time classes.

Both services should include:

```groovy
implementation 'com.fasterxml.jackson.datatype:jackson-datatype-jsr310'
```

and register:

```java
new JavaTimeModule()
```

This allows JSON like:

```json
{
  "processedAt": "2026-07-16T09:30:25.194Z"
}
```

instead of failing with:

```text
Java 8 date/time type `java.time.Instant` not supported by default
```

---

## Kafka Serialization

The project disables Java type headers for JSON events:

```properties
spring.kafka.producer.properties[spring.json.add.type.headers]=false
```

This keeps services decoupled from each other's Java class names.

The contract between services is JSON, not a shared Java class.

---

## Test Configuration

The test context should not start real Kafka listeners.

For `document-api` tests, use:

```properties
spring.kafka.listener.auto-startup=false
```

in:

```text
document-api/src/test/resources/application.properties
```

This prevents `contextLoads()` from trying to connect to Docker hostname:

```text
kafka:9092
```

when tests are running locally outside Docker.

---

## How to Run

From the project root:

```bash
docker compose up --build -d
```

Check containers:

```bash
docker ps
```

Expected containers:

```text
document-api
document-processor
document-kafka
document-api-postgres
```

Follow logs:

```bash
docker logs -f document-api
```

```bash
docker logs -f document-processor
```

Stop everything:

```bash
docker compose down --remove-orphans
```

---

## Build Services

Build `document-api`:

```bash
cd document-api
./gradlew clean build
```

Build `document-processor`:

```bash
cd document-processor
./gradlew clean build
```

If you only want to validate compilation while working around a temporary test setup issue:

```bash
./gradlew clean build -x test
```

---

## Test the Full Flow

Create a file:

```bash
printf "Closing the loop\nDocument API will update PostgreSQL" > phase-6-5.txt
```

Upload it:

```bash
curl -X POST http://localhost:8080/documents/upload \
  -F "file=@phase-6-5.txt"
```

Initial response:

```json
{
  "id": "generated-document-id",
  "originalFilename": "phase-6-5.txt",
  "contentType": "text/plain",
  "size": 52,
  "status": "UPLOADED",
  "storageKey": "documents/2026/07/16/generated-key-phase-6-5.txt",
  "createdAt": "2026-07-16T..."
}
```

The initial status is expected to be:

```text
UPLOADED
```

Processing is asynchronous.

After the processor publishes `DocumentProcessed`, the API consumes the event and updates PostgreSQL.

---

## Inspect Kafka Topics

Consume uploaded events:

```bash
docker exec -it document-kafka \
  /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka:9092 \
  --topic document.uploaded.v1 \
  --from-beginning \
  --property print.key=true \
  --property key.separator=" | "
```

Consume processed events:

```bash
docker exec -it document-kafka \
  /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka:9092 \
  --topic document.processed.v1 \
  --from-beginning \
  --property print.key=true \
  --property key.separator=" | "
```

Expected processed message:

```text
<documentId> | {"eventId":"...","documentId":"...","characterCount":60,"wordCount":9,"lineCount":2,"processedAt":"..."}
```

---

## Inspect Consumer Groups

List consumer groups:

```bash
docker exec -it document-kafka \
  /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server kafka:9092 \
  --list
```

Expected groups:

```text
document-processor
document-api
```

Describe the processor group:

```bash
docker exec -it document-kafka \
  /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server kafka:9092 \
  --describe \
  --group document-processor
```

Describe the API group:

```bash
docker exec -it document-kafka \
  /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server kafka:9092 \
  --describe \
  --group document-api
```

Important columns:

| Column | Meaning |
|---|---|
| `CURRENT-OFFSET` | Last consumed offset for the group |
| `LOG-END-OFFSET` | Latest available offset in the topic |
| `LAG` | Number of pending messages |

When:

```text
LAG = 0
```

the consumer group is caught up.

---

## Inspect PostgreSQL

Enter the database:

```bash
docker exec -it document-api-postgres \
  psql -U postgres -d documentdb
```

List recent documents:

```sql
SELECT
    id,
    original_filename,
    status,
    storage_key,
    created_at
FROM documents
ORDER BY created_at DESC
LIMIT 5;
```

For a specific document:

```sql
SELECT
    id,
    original_filename,
    status,
    storage_key,
    created_at
FROM documents
WHERE id = 'PASTE_DOCUMENT_ID_HERE';
```

Expected final status after the async flow completes:

```text
PROCESSED
```

Exit PostgreSQL:

```sql
\q
```

---

## Current Milestones

### Phase 1 — Project Foundation

```text
[✓] Monorepo structure
[✓] document-api created
[✓] Java and Spring Boot configured
[✓] Docker support
```

### Phase 2 — PostgreSQL and Persistence

```text
[✓] PostgreSQL in Docker
[✓] JPA/Hibernate
[✓] Flyway migrations
[✓] Document metadata persistence
```

### Phase 3 — DDD and REST API

```text
[✓] DDD/Clean Architecture-inspired structure
[✓] Document domain model
[✓] Application use case
[✓] Persistence port and adapter
[✓] POST /documents/upload
```

### Phase 4 — Object Storage

```text
[✓] Magalu Cloud Object Storage
[✓] AWS SDK for Java 2.x
[✓] S3-compatible configuration
[✓] Unique storage keys
[✓] Private object storage
[✓] storageKey persistence
```

### Phase 5 — Kafka Producer

```text
[✓] Kafka added to Docker Compose
[✓] DocumentUploaded event
[✓] document.uploaded.v1 topic
[✓] Kafka producer in document-api
[✓] document ID used as message key
```

### Phase 6.1 — First Kafka Consumer

```text
[✓] document-processor microservice
[✓] Independent Gradle project
[✓] Independent Docker container
[✓] Consumer group document-processor
[✓] Consume document.uploaded.v1
[✓] Deserialize DocumentUploaded
```

### Phase 6.2 — Download From Object Storage

```text
[✓] Processor receives storageKey
[✓] Processor downloads file from Object Storage
[✓] Processor reads .txt as UTF-8
[✓] Processor remains independent from PostgreSQL
```

### Phase 6.3 — Text Processing

```text
[✓] TextDocumentProcessor
[✓] ProcessedText
[✓] characterCount
[✓] wordCount
[✓] lineCount
```

### Phase 6.4 — Publish DocumentProcessed

```text
[✓] DocumentProcessedEvent
[✓] DocumentProcessedPublisher port
[✓] KafkaDocumentProcessedPublisher adapter
[✓] document.processed.v1 topic
[✓] Processor becomes consumer + producer
```

### Phase 6.5 — Consume DocumentProcessed and Update PostgreSQL

```text
[✓] DocumentProcessedKafkaConsumer in document-api
[✓] MarkDocumentAsProcessedCommand
[✓] MarkDocumentAsProcessedUseCase
[✓] Document.markAsProcessed()
[✓] DocumentRepository.findById()
[✓] PostgreSQL status update to PROCESSED
```

---

## Important Architectural Concepts Learned

### Kafka Consumer

The processor consumes events from:

```text
document.uploaded.v1
```

### Consumer Groups

The processor uses:

```text
document-processor
```

The API uses:

```text
document-api
```

Each service has its own independent Kafka offset tracking.

### Event-Driven Communication

Services communicate by events, not by direct HTTP calls.

### Eventual Consistency

Immediately after upload:

```text
status = UPLOADED
```

After asynchronous processing finishes:

```text
status = PROCESSED
```

There is a small delay between these states. That is expected.

### Service Independence

The processor does not access the API database.

The API does not execute processing logic.

### Ports and Adapters

Examples:

```text
DocumentStorage -> MagaluDocumentStorage
DocumentProcessedPublisher -> KafkaDocumentProcessedPublisher
DocumentRepository -> JpaDocumentRepositoryAdapter
```

---

## Next Steps

The next logical step is **idempotency**.

Now that the full async cycle exists, we need to answer:

```text
What happens if the same DocumentProcessed event is delivered twice?
```

Possible improvements:

```text
Phase 7
├── Idempotency for DocumentProcessed consumption
├── Ignore already PROCESSED documents
├── Better status transition rules
└── Safe repeated event handling

Phase 8
├── Retry strategy
├── Backoff
└── transient error handling

Phase 9
├── Dead-letter topic
└── failed event investigation

Phase 10
├── Outbox pattern
└── reliable event publishing from document-api
```

---

## Notes

Opening:

```text
http://localhost:8080
```

returns `404 Not Found`.

This is expected because the API currently exposes:

```text
POST /documents/upload
```

and does not define a root `/` endpoint.

---

## Summary

The project now demonstrates a complete event-driven document processing cycle:

```text
upload
  |
  v
metadata persistence
  |
  v
object storage
  |
  v
DocumentUploaded
  |
  v
Kafka
  |
  v
processor
  |
  v
text analysis
  |
  v
DocumentProcessed
  |
  v
Kafka
  |
  v
API consumer
  |
  v
PostgreSQL status update
```

This is the foundation for learning production-grade distributed systems patterns such as idempotency, retry, dead-letter topics, eventual consistency, and the outbox pattern.
