# Document Platform

A study project for building a document upload and processing platform with Java, Spring Boot, PostgreSQL, Flyway, Docker, Domain-Driven Design (DDD), Clean Architecture principles, and Magalu Cloud Object Storage.

The project currently contains the `document-api` microservice. It receives multipart file uploads, stores the file in Magalu Cloud Object Storage, persists document metadata in PostgreSQL, and returns the generated storage key.

## Current Status

The project currently supports:

- Monorepo structure
- `document-api` microservice
- Spring Boot REST API
- PostgreSQL
- Flyway database migrations
- JPA/Hibernate
- Docker and Docker Compose
- DDD/Clean Architecture-inspired package structure
- Multipart file upload
- Magalu Cloud Object Storage integration
- AWS SDK for Java 2.x
- S3-compatible endpoint configuration
- Private document storage
- Storage key persistence in PostgreSQL

Kafka and the `document-processor` microservice are planned for the next phases.

## Current Architecture

```text
Client
  |
  | POST /documents/upload
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
DocumentStorage             DocumentRepository
  |                             |
  v                             v
MagaluDocumentStorage       JpaDocumentRepositoryAdapter
  |                             |
  v                             v
Magalu Cloud                PostgreSQL
Object Storage
```

The upload flow is:

```text
1. Receive a multipart file
2. Generate a unique storage key
3. Upload the file to Magalu Cloud Object Storage
4. Create the Document domain object
5. Persist metadata and storage key in PostgreSQL
6. Return the upload result
```

## Project Structure

```text
document-platform
├── README.md
├── docker-compose.yml
├── .env.example
├── .gitignore
└── document-api
    ├── Dockerfile
    ├── build.gradle
    ├── settings.gradle
    ├── gradlew
    ├── gradle
    └── src
        └── main
            ├── java
            │   └── io
            │       └── lossantis
            │           └── documentapi
            │               ├── DocumentApiApplication.java
            │               └── document
            │                   ├── application
            │                   │   ├── UploadDocumentCommand.java
            │                   │   ├── UploadDocumentResult.java
            │                   │   ├── UploadDocumentUseCase.java
            │                   │   └── storage
            │                   │       ├── DocumentStorage.java
            │                   │       └── DocumentStorageKeyGenerator.java
            │                   ├── domain
            │                   │   ├── model
            │                   │   │   ├── Document.java
            │                   │   │   └── DocumentStatus.java
            │                   │   └── repository
            │                   │       └── DocumentRepository.java
            │                   ├── infrastructure
            │                   │   ├── persistence
            │                   │   │   ├── DocumentJpaEntity.java
            │                   │   │   ├── JpaDocumentRepositoryAdapter.java
            │                   │   │   └── SpringDataDocumentRepository.java
            │                   │   └── storage
            │                   │       ├── MagaluDocumentStorage.java
            │                   │       ├── MagaluObjectStorageConfig.java
            │                   │       └── MagaluObjectStorageProperties.java
            │                   └── presentation
            │                       └── DocumentController.java
            └── resources
                ├── application.properties
                └── db
                    └── migration
                        ├── V1__create_documents_table.sql
                        └── V2__add_storage_key_to_documents.sql
```

> Package paths may evolve as the project grows, but the architectural responsibilities remain the same.

## Layer Responsibilities

### Presentation

Contains HTTP controllers.

Main class:

```text
DocumentController
```

Responsibilities:

- Receive HTTP requests
- Read the multipart file
- Create an application command
- Call the upload use case
- Return the HTTP response

Current endpoint:

```http
POST /documents/upload
```

### Application

Contains use cases and application ports.

Main classes:

```text
UploadDocumentCommand
UploadDocumentResult
UploadDocumentUseCase
DocumentStorage
DocumentStorageKeyGenerator
```

Responsibilities:

- Orchestrate the upload flow
- Send file content to the storage port
- Receive the generated storage key
- Create the domain object
- Persist document metadata
- Return the result

The application layer depends on abstractions instead of a specific storage provider.

### Domain

Contains the business model.

Main classes:

```text
Document
DocumentStatus
DocumentRepository
```

Responsibilities:

- Represent the `Document` business concept
- Define document status
- Define the persistence port
- Create new uploaded documents
- Restore persisted documents

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

Contains technical implementations.

Persistence classes:

```text
DocumentJpaEntity
SpringDataDocumentRepository
JpaDocumentRepositoryAdapter
```

Storage classes:

```text
MagaluDocumentStorage
MagaluObjectStorageConfig
MagaluObjectStorageProperties
```

Responsibilities:

- Map the `documents` table
- Integrate with Spring Data JPA
- Implement `DocumentRepository`
- Convert between domain and persistence models
- Configure the S3-compatible client
- Upload files to Magalu Cloud Object Storage
- Implement the `DocumentStorage` application port

## Document Upload Flow

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
        v
DocumentStorage.upload(...)
        |
        v
MagaluDocumentStorage
        |
        v
Magalu Cloud Object Storage
        |
        | returns storageKey
        v
Document.upload(..., storageKey)
        |
        v
DocumentRepository.save(...)
        |
        v
JpaDocumentRepositoryAdapter
        |
        v
SpringDataDocumentRepository
        |
        v
PostgreSQL
```

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

Example configuration concept:

```java
S3Client.builder()
    .endpointOverride(URI.create(endpoint))
    .region(Region.of(region))
    .credentialsProvider(credentialsProvider)
    .forcePathStyle(true)
    .build();
```

### Storage Key Format

Every uploaded file receives a generated storage key.

Example:

```text
documents/2026/06/29/b14adc8f-34bd-4b8a-928d-f8c4df8f8b85-test2.txt
```

The format is:

```text
documents/YYYY/MM/DD/UUID-original-filename
```

This avoids filename collisions and keeps objects organized by date.

### Access Model

Objects are stored privately.

Bucket permissions are managed outside the application as infrastructure configuration.

The application does not change bucket ACLs or bucket policies at startup.

For private files, temporary access can be granted with a presigned URL.

Example with MGC CLI:

```bash
mgc object-storage objects presign \
  --dst="document-platform/documents/2026/06/29/example.txt"
```

This generates a temporary signed URL that can be opened in a browser.

## Database

PostgreSQL runs locally through Docker Compose.

The `documents` table is managed by Flyway migrations.

### V1 - Create Documents Table

```text
document-api/src/main/resources/db/migration/V1__create_documents_table.sql
```

Fields:

```text
id
original_filename
content_type
size
status
created_at
```

### V2 - Add Storage Key

```text
document-api/src/main/resources/db/migration/V2__add_storage_key_to_documents.sql
```

Adds:

```text
storage_key
```

Current document metadata:

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

This table records executed migrations.

## Environment Variables

Create a `.env` file at the project root.

Example:

```env
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

## Security

Do not commit real cloud credentials.

Keep the following file ignored:

```text
.env
```

Recommended `.gitignore` entry:

```gitignore
.env
```

Cloud access keys and secret keys must be treated as credentials.

If a credential is exposed publicly or committed to Git history, revoke it and create a new one.

## Docker Compose

The local environment contains:

```text
Docker Compose
├── document-api
└── PostgreSQL
```

The `document-api` container receives database and Object Storage configuration through environment variables.

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
document-api-postgres
```

Check API logs:

```bash
docker logs -f document-api
```

Or:

```bash
docker compose logs -f document-api
```

Stop the environment:

```bash
docker compose down
```

Stop and remove orphan containers:

```bash
docker compose down --remove-orphans
```

## Rebuild vs Recreate

When Java code, dependencies, migrations, or the Dockerfile change:

```bash
docker compose up --build
```

When only `.env` values change, rebuild is not required. Recreate the container instead:

```bash
docker compose up -d --force-recreate document-api
```

## Test the Upload Endpoint

Create a test file:

```bash
echo "test object storage" > test.txt
```

Upload it:

```bash
curl -X POST http://localhost:8080/documents/upload \
  -F "file=@test.txt"
```

Expected response:

```json
{
  "id": "generated-uuid",
  "originalFilename": "test.txt",
  "contentType": "text/plain",
  "size": 20,
  "status": "UPLOADED",
  "storageKey": "documents/2026/06/29/generated-uuid-test.txt",
  "createdAt": "2026-06-29T..."
}
```

A successful response confirms that the API:

```text
1. received the file
2. uploaded it to Object Storage
3. generated a storage key
4. saved metadata in PostgreSQL
```

## Verify the Object in Magalu Cloud

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
mgc object-storage objects list document-platform/documents/2026/06/29
```

## Open a Private File in the Browser

Generate a presigned URL:

```bash
mgc object-storage objects presign \
  --dst="document-platform/YOUR_STORAGE_KEY"
```

Example:

```bash
mgc object-storage objects presign \
  --dst="document-platform/documents/2026/06/29/example.txt"
```

Copy the generated URL and open it in a browser.

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

```sql
\q
```

## About `localhost:8080`

Opening:

```text
http://localhost:8080
```

returns `404 Not Found`.

This is expected because there is no endpoint mapped to `/`.

The currently available endpoint is:

```http
POST /documents/upload
```

## Local vs Magalu Cloud

Current local environment:

```text
Developer Machine
└── Docker Compose
    ├── document-api
    └── PostgreSQL
```

Current external cloud dependency:

```text
Magalu Cloud
└── Object Storage
    └── document-platform bucket
```

Target architecture will evolve toward:

```text
Magalu Cloud

VM document-api
└── Docker
    └── document-api container

PostgreSQL

Object Storage

Kafka

VM document-processor
└── Docker
    └── document-processor container
```

## Next Phases

Planned evolution:

```text
Phase 5
├── Publish DocumentUploaded event to Kafka
└── Include documentId, storageKey, contentType, and size

Phase 6
├── Create document-processor microservice
├── Consume DocumentUploaded
├── Download the file from Object Storage
├── Validate type and size
├── Process the document
└── Update document status

Later
├── Error handling
├── Retry strategy
├── Dead-letter topics
├── Observability
├── Automated tests
├── CI/CD
└── Deployment to separate Magalu Cloud VMs
```

## Technology Stack

```text
Java 25
Spring Boot
Spring Web
Spring Data JPA
PostgreSQL
Flyway
AWS SDK for Java 2.x
Magalu Cloud Object Storage
Docker
Docker Compose
Gradle
```

## Current Milestone

The current milestone is complete when all of the following work:

```text
[✓] document-api runs in Docker
[✓] PostgreSQL runs in Docker
[✓] Flyway creates and migrates the database schema
[✓] POST /documents/upload accepts multipart files
[✓] File is uploaded to Magalu Cloud Object Storage
[✓] storageKey is generated
[✓] Document metadata is saved in PostgreSQL
[✓] Private files can be accessed with presigned URLs
```

The next major milestone is Kafka integration.

