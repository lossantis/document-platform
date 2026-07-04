# Document Platform

A study project for building an event-driven document upload and processing platform with Java, Spring Boot, Apache Kafka, PostgreSQL, Flyway, Docker, Domain-Driven Design (DDD), Clean Architecture principles, and Magalu Cloud Object Storage.

The project currently contains two independent microservices:

- `document-api`: receives document uploads, stores files in Magalu Cloud Object Storage, persists metadata in PostgreSQL, and publishes a `DocumentUploaded` event to Kafka.
- `document-processor`: consumes `DocumentUploaded` events from Kafka as part of the asynchronous document processing pipeline.

The current milestone completes the first end-to-end asynchronous communication between the two services.

---

## Current Status

The project currently supports:

- Monorepo structure
- Two independent Spring Boot microservices
- `document-api`
- `document-processor`
- REST API for multipart document upload
- PostgreSQL
- Flyway database migrations
- JPA/Hibernate
- Magalu Cloud Object Storage
- AWS SDK for Java 2.x
- S3-compatible endpoint configuration
- Private document storage
- Apache Kafka
- `DocumentUploaded` event publishing
- Kafka consumer groups
- `DocumentUploaded` event consumption
- JSON event deserialization
- Docker and Docker Compose
- DDD/Clean Architecture-inspired package structure

The current asynchronous flow is:

```text
document-api
    |
    | publishes DocumentUploaded
    v
Kafka
    |
    | topic: document.uploaded.v1
    v
document-processor
    |
    | deserializes the event
    v
Application logs
```

The `document-processor` does not yet download or process the document. That will be added progressively in the next steps.

---

## Architecture

```text
                            CLIENT
                              |
                              | POST /documents/upload
                              v
+-------------------------------------------------------------+
|                        document-api                         |
|                                                             |
|  1. Receives multipart file                                 |
|  2. Generates a storage key                                 |
|  3. Uploads file to Object Storage                          |
|  4. Persists document metadata in PostgreSQL                |
|  5. Publishes DocumentUploaded                              |
+-----------------------------+-------------------------------+
                              |
                              | document.uploaded.v1
                              v
                    +-------------------+
                    |       Kafka       |
                    +---------+---------+
                              |
                              | Consumer group:
                              | document-processor
                              v
+-------------------------------------------------------------+
|                    document-processor                       |
|                                                             |
|  1. Consumes DocumentUploaded                               |
|  2. Receives the message as JSON                            |
|  3. Deserializes it into DocumentUploadedEvent              |
|  4. Logs the event data                                     |
+-------------------------------------------------------------+
```

The target event-driven flow is:

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

This architecture is being built incrementally to explore:

- Kafka consumers
- Consumer groups
- Event-driven communication
- Idempotency
- Retry strategies
- Dead-letter topics
- Eventual consistency
- Outbox pattern

---

## Microservices

### `document-api`

Responsibilities:

- Expose the document upload HTTP endpoint
- Receive multipart files
- Generate unique storage keys
- Upload files to Magalu Cloud Object Storage
- Persist document metadata in PostgreSQL
- Publish `DocumentUploaded` events to Kafka

The `document-api` owns the document metadata database and is the only service that currently accesses PostgreSQL.

### `document-processor`

Current responsibilities:

- Connect to Kafka
- Join the `document-processor` consumer group
- Consume messages from `document.uploaded.v1`
- Deserialize JSON into `DocumentUploadedEvent`
- Log the received event

The `document-processor` currently does **not**:

- Access PostgreSQL
- Access the `document-api` database
- Download files from Object Storage
- Process `.txt` files
- Publish `DocumentProcessed`

Those responsibilities will be introduced in the next phases.

---

## Service Independence

The two microservices are independent applications.

```text
document-api
├── own Gradle project
├── own Dockerfile
├── own Spring Boot application
└── owns PostgreSQL document metadata

document-processor
├── own Gradle project
├── own Dockerfile
├── own Spring Boot application
└── no database dependency
```

The `document-processor` does not depend on the `document-api` Java project.

The services communicate through Kafka events rather than direct Java dependencies or shared database access.

```text
document-api
    |
    | JSON event
    v
Kafka
    |
    | JSON event
    v
document-processor
```

The contract between the services is the event payload, not a shared Java class.

---

## Current Upload and Event Flow

```text
POST /documents/upload
        |
        v
DocumentController
        |
        v
UploadDocumentCommand
        |
        v
UploadDocumentUseCase
        |
        +-----------------------------+
        |                             |
        v                             v
DocumentStorage                 DocumentRepository
        |                             |
        v                             v
MagaluDocumentStorage       JpaDocumentRepositoryAdapter
        |                             |
        v                             v
Magalu Cloud Object Storage      PostgreSQL
        |
        v
DocumentUploadedPublisher
        |
        v
KafkaDocumentUploadedPublisher
        |
        v
document.uploaded.v1
        |
        v
DocumentUploadedKafkaConsumer
        |
        v
ObjectMapper
        |
        v
DocumentUploadedEvent
        |
        v
Application logs
```

The complete current flow is:

1. The client sends a multipart file.
2. `document-api` generates a unique storage key.
3. The file is uploaded to Magalu Cloud Object Storage.
4. A `Document` domain object is created.
5. Metadata and the storage key are persisted in PostgreSQL.
6. `document-api` publishes `DocumentUploaded` to Kafka.
7. Kafka stores the event in `document.uploaded.v1`.
8. `document-processor` consumes the event as part of the `document-processor` consumer group.
9. The JSON payload is deserialized into `DocumentUploadedEvent`.
10. The event data is written to the processor logs.

---

## Kafka

Apache Kafka is used for asynchronous communication between the microservices.

### Current Topic

```text
document.uploaded.v1
```

The topic name includes a version suffix:

```text
.v1
```

This makes the event contract version explicit and allows future incompatible event versions to use a different topic or contract.

### Producer

```text
document-api
```

Publishes:

```text
DocumentUploaded
```

### Consumer

```text
document-processor
```

Consumes:

```text
DocumentUploaded
```

### Consumer Group

```text
document-processor
```

Conceptually:

```text
Topic: document.uploaded.v1
            |
            v
Consumer Group: document-processor
            |
            v
document-processor instance
```

Kafka stores offsets per consumer group. After the processor consumes a message, stops, and starts again, it can continue from the previously committed position instead of consuming every old message again.

---

## `DocumentUploaded` Event

Example payload:

```json
{
  "eventId": "271e0a85-1b8b-4ac4-8597-1568eb7cee2f",
  "documentId": "8f6e110d-8083-4e50-88d7-3942c695d590",
  "storageKey": "documents/2026/07/04/52c...-example.txt",
  "contentType": "text/plain",
  "size": 128
}
```

Fields:

| Field | Description |
|---|---|
| `eventId` | Unique identifier for the event |
| `documentId` | Identifier of the uploaded document |
| `storageKey` | Object Storage key used to locate the file |
| `contentType` | MIME type of the uploaded file |
| `size` | File size in bytes |

The Kafka message key is the document ID.

```text
KEY                                      VALUE

8f6e110d-8083-4e50-88d7-3942c695d590 | {"eventId":"...", ...}
```

Using the document ID as the key prepares the system for future partitioning strategies where events related to the same document should preserve ordering.

---

## Event Deserialization

The producer publishes JSON without Java type headers.

The processor receives the Kafka value as a string:

```text
Kafka
  |
  v
StringDeserializer
  |
  v
JSON String
  |
  v
ObjectMapper
  |
  v
DocumentUploadedEvent
```

This avoids coupling the consumer to the producer's Java package or class name.

The services independently represent the same JSON event contract.

---

## Project Structure

```text
document-platform
├── README.md
├── docker-compose.yml
├── .env.example
├── .gitignore
│
├── document-api
│   ├── Dockerfile
│   ├── build.gradle
│   ├── settings.gradle
│   ├── gradlew
│   ├── gradle
│   └── src
│       └── main
│           ├── java
│           │   └── io/lossantis/documentapi
│           │       ├── DocumentApiApplication.java
│           │       └── document
│           │           ├── application
│           │           ├── domain
│           │           ├── infrastructure
│           │           │   ├── messaging
│           │           │   ├── persistence
│           │           │   └── storage
│           │           └── presentation
│           └── resources
│               ├── application.properties
│               └── db/migration
│
└── document-processor
    ├── Dockerfile
    ├── build.gradle
    ├── settings.gradle
    ├── gradlew
    ├── gradle
    └── src
        └── main
            ├── java
            │   └── io/lossantis/documentprocessor
            │       ├── DocumentProcessorApplication.java
            │       └── document
            │           └── infrastructure
            │               └── messaging
            │                   ├── DocumentUploadedEvent.java
            │                   └── DocumentUploadedKafkaConsumer.java
            └── resources
                └── application.properties
```

> Package paths may evolve as the project grows, but the architectural responsibilities remain the same.

---

## `document-api` Architecture

The `document-api` follows a DDD/Clean Architecture-inspired package structure.

### Presentation

Contains HTTP controllers.

Main responsibility:

```text
HTTP request -> application command -> use case -> HTTP response
```

Current endpoint:

```text
POST /documents/upload
```

### Application

Contains use cases and application ports.

Responsibilities:

- Orchestrate the upload flow
- Send file content to the storage port
- Create the domain object
- Persist document metadata
- Publish the upload event
- Return the upload result

The application layer depends on abstractions instead of infrastructure implementations.

### Domain

Main concepts:

```text
Document
DocumentStatus
DocumentRepository
```

The domain does not depend on:

```text
Spring
JPA
PostgreSQL
Docker
Flyway
AWS SDK
Magalu Cloud
Kafka
```

### Infrastructure

Current responsibilities:

- PostgreSQL persistence
- Spring Data JPA integration
- Domain/persistence mapping
- S3-compatible Object Storage configuration
- Magalu Cloud Object Storage upload
- Kafka event publishing

---

## `document-processor` Architecture

The processor is intentionally simple in the current phase.

```text
document-processor
    |
    v
infrastructure
    |
    v
messaging
    ├── DocumentUploadedEvent
    └── DocumentUploadedKafkaConsumer
```

There is no application or domain layer yet because the processor currently has no processing business rule.

At this stage, it only:

```text
receive
  |
  v
deserialize
  |
  v
log
```

The application and domain layers will be introduced when real processing behavior is added.

---

## Object Storage

The project uses Magalu Cloud Object Storage through its S3-compatible API.

The Java application uses:

```text
AWS SDK for Java 2.x
software.amazon.awssdk:s3
```

The S3 client is configured with:

- Magalu Cloud Object Storage endpoint
- Region
- Access key
- Secret key
- Path-style access

### Storage Key Format

Example:

```text
documents/2026/07/04/b14adc8f-34bd-4b8a-928d-f8c4df8f8b85-example.txt
```

Format:

```text
documents/YYYY/MM/DD/UUID-original-filename
```

This avoids filename collisions and organizes objects by date.

### Access Model

Objects are stored privately.

Bucket permissions are managed outside the application as infrastructure configuration.

The application does not change bucket ACLs or bucket policies at startup.

For private files, temporary access can be granted with a presigned URL:

```bash
mgc object-storage objects presign \
  --dst="document-platform/YOUR_STORAGE_KEY"
```

---

## Database

PostgreSQL runs locally through Docker Compose.

The `documents` table is managed by Flyway migrations.

Current metadata includes:

```text
id
original_filename
content_type
size
status
storage_key
created_at
```

Flyway also manages:

```text
flyway_schema_history
```

Only `document-api` accesses this database.

The `document-processor` does not connect to PostgreSQL.

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

The `.env` file must never be committed.

The repository should contain only `.env.example` with placeholder values.

---

## Security

Do not commit real cloud credentials.

Keep the following file ignored:

```text
.env
```

Cloud access keys and secret keys must be treated as credentials.

If a credential is exposed publicly or committed to Git history, revoke it and create a new one.

Objects in the bucket remain private by default.

The application does not automatically make the bucket or uploaded objects public.

---

## Docker Compose

The local environment contains:

```text
Docker Compose
├── document-api
├── document-processor
├── Kafka
└── PostgreSQL
```

External cloud dependency:

```text
Magalu Cloud
└── Object Storage
```

Communication inside Docker:

```text
document-api       --> kafka:9092
document-processor --> kafka:9092
document-api       --> postgres:5432
```

The processor has no database connection.

---

## How to Run

From the project root:

```bash
docker compose up --build
```

Run in detached mode:

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

Check API logs:

```bash
docker compose logs -f document-api
```

Check processor logs:

```bash
docker compose logs -f document-processor
```

Stop the environment:

```bash
docker compose down
```

Stop and remove orphan containers:

```bash
docker compose down --remove-orphans
```

---

## Build Individual Services

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

---

## Test the Complete Current Flow

Create a text file:

```bash
echo "Hello from document processor" > test-processor.txt
```

Upload it:

```bash
curl -X POST http://localhost:8080/documents/upload \
  -F "file=@test-processor.txt"
```

A successful request confirms that `document-api`:

1. Received the file.
2. Uploaded it to Object Storage.
3. Generated a storage key.
4. Saved metadata in PostgreSQL.
5. Published `DocumentUploaded` to Kafka.

Follow the processor logs:

```bash
docker compose logs -f document-processor
```

Expected output:

```text
========================================
DocumentUploaded received
eventId:     ...
documentId:  ...
storageKey:  documents/2026/07/04/...-test-processor.txt
contentType: text/plain
size:        ...
========================================
```

This confirms the current end-to-end asynchronous flow:

```text
HTTP upload
    |
    v
document-api
    |
    v
Object Storage + PostgreSQL
    |
    v
Kafka
    |
    v
document-processor
    |
    v
log
```

---

## Inspect Kafka Messages

Consume the topic from the beginning:

```bash
docker exec -it document-kafka \
  /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka:9092 \
  --topic document.uploaded.v1 \
  --from-beginning \
  --property print.key=true \
  --property key.separator=" | "
```

Expected format:

```text
document-id | {"eventId":"...","documentId":"...","storageKey":"...","contentType":"text/plain","size":...}
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

Expected group:

```text
document-processor
```

Describe the processor consumer group:

```bash
docker exec -it document-kafka \
  /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server kafka:9092 \
  --describe \
  --group document-processor
```

Example output:

```text
GROUP               TOPIC                  PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG
document-processor  document.uploaded.v1   0          5               5               0
```

Meaning:

| Column | Description |
|---|---|
| `CURRENT-OFFSET` | Position already consumed by the group |
| `LOG-END-OFFSET` | Latest available position in the partition |
| `LAG` | Number of messages still waiting to be consumed |

When `LAG = 0`, the consumer group is caught up.

---

## Kafka Offset Behavior

The processor uses:

```properties
spring.kafka.consumer.auto-offset-reset=earliest
```

For a new consumer group:

```text
group has no committed offset
        |
        v
earliest
        |
        v
consume from the oldest available message
```

After offsets have been committed:

```text
processor stops
        |
        v
processor starts again
        |
        v
Kafka restores the group's position
        |
        v
processing continues from the stored offset
```

The processor does not restart from the beginning every time.

---

## Access PostgreSQL

Enter the PostgreSQL container:

```bash
docker exec -it document-api-postgres \
  psql -U postgres -d documentdb
```

List tables:

```sql
\dt
```

Query documents:

```sql
SELECT
    id,
    original_filename,
    content_type,
    size,
    status,
    storage_key,
    created_at
FROM documents;
```

Check Flyway migrations:

```sql
SELECT *
FROM flyway_schema_history;
```

Exit:

```text
\q
```

---

## Verify Objects in Magalu Cloud

List the bucket root:

```bash
mgc object-storage objects list document-platform
```

Because Object Storage uses prefixes, the result may show:

```text
CommonPrefixes:
- Path: documents/
Contents: []
```

This does not mean the bucket is empty.

List a deeper path:

```bash
mgc object-storage objects list document-platform/documents/2026/07/04
```

Generate a temporary URL for a private object:

```bash
mgc object-storage objects presign \
  --dst="document-platform/YOUR_STORAGE_KEY"
```

---

## About `localhost:8080`

Opening:

```text
http://localhost:8080
```

returns:

```text
404 Not Found
```

This is expected because there is no endpoint mapped to `/`.

The currently available endpoint is:

```text
POST /documents/upload
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
[✓] Kafka producer
[✓] document ID used as message key
[✓] Event published after upload flow
```

### Phase 6.1 — First Kafka Consumer

```text
[✓] document-processor microservice
[✓] Independent Gradle project
[✓] Independent Docker container
[✓] Kafka connection
[✓] document-processor consumer group
[✓] Consume document.uploaded.v1
[✓] Receive message as JSON string
[✓] Deserialize into DocumentUploadedEvent
[✓] Log documentId and storageKey
```

---

## Next Phase

The next step is to make `document-processor` use the event's `storageKey` to download the uploaded `.txt` file from Magalu Cloud Object Storage.

Target flow:

```text
document.uploaded.v1
        |
        v
DocumentUploadedKafkaConsumer
        |
        v
ProcessDocumentUseCase
        |
        v
DocumentStorage.download(storageKey)
        |
        v
Magalu Cloud Object Storage
        |
        v
.txt content
```

The next implementation should introduce:

```text
application
├── ProcessDocumentUseCase
└── storage
    └── DocumentStorage

infrastructure
├── messaging
│   └── DocumentUploadedKafkaConsumer
└── storage
    └── MagaluDocumentStorage
```

After that, the project will evolve toward:

```text
download .txt
    |
    v
process text
    |
    v
publish DocumentProcessed
    |
    v
document.processed.v1
    |
    v
document-api consumes result
    |
    v
update PostgreSQL
```

---

## Planned Evolution

```text
Phase 6.2
├── Connect document-processor to Object Storage
├── Download document by storageKey
└── Read .txt content

Phase 6.3
├── Process text
├── Count characters
├── Count words
└── Count lines

Phase 6.4
├── Create DocumentProcessed event
└── Publish document.processed.v1

Phase 6.5
├── document-api consumes DocumentProcessed
└── Update document status in PostgreSQL

Later
├── Idempotency
├── Retry strategy
├── Dead-letter topic
├── Eventual consistency analysis
├── Outbox pattern
├── Observability
├── Automated tests
├── CI/CD
└── Deployment to separate Magalu Cloud VMs
```

---

## Technology Stack

```text
Java 25
Spring Boot 4.1
Spring Kafka
Apache Kafka 4.1
Spring Web
Spring Data JPA
PostgreSQL 16
Flyway
AWS SDK for Java 2.x
Magalu Cloud Object Storage
Docker
Docker Compose
Gradle 9
```

---

## Local vs Target Architecture

Current environment:

```text
Developer Machine
└── Docker Compose
    ├── document-api
    ├── document-processor
    ├── Kafka
    └── PostgreSQL

Magalu Cloud
└── Object Storage
```

Target deployment architecture:

```text
Magalu Cloud

VM document-api
└── Docker
    └── document-api

VM document-processor
└── Docker
    └── document-processor

PostgreSQL

Object Storage

Kafka
```

The same event-driven service boundaries used locally are intended to support independent deployment later.

---

## Learning Goals

This project is intentionally developed in small phases.

Rather than introducing every distributed-systems pattern at once, each new problem is introduced when the architecture makes it relevant.

Current progression:

```text
File upload
    |
    v
Persistence
    |
    v
Object Storage
    |
    v
Kafka producer
    |
    v
Kafka consumer
    |
    v
Consumer groups
    |
    v
Event-driven processing
    |
    v
Eventual consistency
    |
    v
Idempotency
    |
    v
Retry
    |
    v
Dead-letter topic
    |
    v
Outbox pattern
```

The goal is not only to build a working platform, but to understand why each architectural pattern becomes necessary.
