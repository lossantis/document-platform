# Document Platform

Study project for building a document upload and processing platform using Java, Spring Boot, PostgreSQL, Flyway, Docker, and a DDD-inspired architecture.

At this stage, the project contains the `document-api` microservice, which receives document uploads and stores document metadata in PostgreSQL.

## Current Project Structure

```text
document-platform
├── README.md
├── docker-compose.yml
├── .env.example
├── document-api
│   ├── Dockerfile
│   ├── build.gradle
│   ├── settings.gradle
│   └── src
│       └── main
│           ├── java
│           │   └── io
│           │       └── lossantis
│           │           └── documentapi
│           │               ├── DocumentApiApplication.java
│           │               └── document
│           │                   ├── domain
│           │                   ├── application
│           │                   ├── infrastructure
│           │                   └── presentation
│           └── resources
│               ├── application.properties
│               └── db
│                   └── migration
│                       └── V1__create_documents_table.sql
├── document-processor
├── docs
└── infra
```

## What Is Already Working

The project currently has:

- Monorepo structure with Git initialized at the `document-platform` root
- `document-api` microservice
- Dockerfile for running `document-api` inside a container
- Docker Compose at the project root
- PostgreSQL running in a local Docker container
- Spring Boot running in a Docker container
- Flyway creating the `documents` table
- JPA/Hibernate validating the database schema
- REST endpoint for document upload
- Document metadata persistence in PostgreSQL

## Current Architecture

The `document-api` follows a DDD/Clean Architecture-inspired structure:

```text
document
├── domain
│   ├── model
│   │   ├── Document.java
│   │   └── DocumentStatus.java
│   └── repository
│       └── DocumentRepository.java
│
├── application
│   ├── command
│   │   └── UploadDocumentCommand.java
│   ├── result
│   │   └── UploadDocumentResult.java
│   └── usecase
│       └── UploadDocumentUseCase.java
│
├── infrastructure
│   └── persistence
│       ├── DocumentJpaEntity.java
│       ├── SpringDataDocumentRepository.java
│       └── JpaDocumentRepositoryAdapter.java
│
└── presentation
    └── rest
        └── DocumentController.java
```

## Layer Responsibilities

### Presentation

Contains the REST controllers.

Main class:

```text
DocumentController
```

Responsibilities:

- Receive HTTP requests
- Extract request data
- Convert request data into an application command
- Call the application use case

Current endpoint:

```text
POST /documents/upload
```

### Application

Contains the application use cases.

Main classes:

```text
UploadDocumentCommand
UploadDocumentUseCase
UploadDocumentResult
```

Responsibilities:

- Receive the data required to execute the upload use case
- Orchestrate the document upload flow
- Create the domain object
- Call the repository port
- Return the operation result

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
- Define possible document statuses
- Define the persistence port through the `DocumentRepository` interface

The domain layer does not know anything about Spring, JPA, Docker, PostgreSQL, Flyway, Kafka, or Object Storage.

### Infrastructure

Contains technical implementations.

Main classes:

```text
DocumentJpaEntity
SpringDataDocumentRepository
JpaDocumentRepositoryAdapter
```

Responsibilities:

- Map the `documents` database table
- Integrate with Spring Data JPA
- Implement the `DocumentRepository` domain interface
- Convert between `Document` and `DocumentJpaEntity`

## Current Upload Flow

```text
POST /documents/upload
        ↓
DocumentController
        ↓
UploadDocumentCommand
        ↓
UploadDocumentUseCase
        ↓
Document.upload()
        ↓
DocumentRepository
        ↓
JpaDocumentRepositoryAdapter
        ↓
SpringDataDocumentRepository
        ↓
PostgreSQL
```

At this stage, the uploaded file itself is not stored yet.

Only metadata is saved:

```text
id
original_filename
content_type
size
status
created_at
```

## Database

The local database runs as a PostgreSQL container through Docker Compose.

The `documents` table is created by Flyway using the following migration:

```text
document-api/src/main/resources/db/migration/V1__create_documents_table.sql
```

Current migration:

```sql
CREATE TABLE documents (
    id UUID PRIMARY KEY,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    size BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
```

Flyway also creates the following table automatically:

```text
flyway_schema_history
```

This table tracks which migrations have already been executed.

## Application Configuration

The main configuration file is located at:

```text
document-api/src/main/resources/application.properties
```

Current configuration:

```properties
spring.application.name=document-api

server.port=8080

spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

Hibernate does not create or update database tables automatically.

Database creation and schema changes are handled by Flyway.

## Environment Variables

Create a `.env` file at the project root:

```text
document-platform/.env
```

Example for local development with PostgreSQL running inside Docker Compose:

```env
DOCUMENT_API_DATASOURCE_URL=jdbc:postgresql://postgres:5432/documentdb
DOCUMENT_API_DATASOURCE_USERNAME=postgres
DOCUMENT_API_DATASOURCE_PASSWORD=postgres
```

The `.env` file must not be committed to Git.

Create a `.env.example` file with example values:

```env
DOCUMENT_API_DATASOURCE_URL=jdbc:postgresql://postgres:5432/documentdb
DOCUMENT_API_DATASOURCE_USERNAME=postgres
DOCUMENT_API_DATASOURCE_PASSWORD=change-me
```

## Docker Compose

The `docker-compose.yml` file is located at the project root:

```text
document-platform/docker-compose.yml
```

Current configuration:

```yaml
services:
  postgres:
    image: postgres:16
    container_name: document-api-postgres
    environment:
      POSTGRES_DB: documentdb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - document-api-postgres-data:/var/lib/postgresql/data

  document-api:
    build:
      context: ./document-api
    container_name: document-api
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: ${DOCUMENT_API_DATASOURCE_URL}
      SPRING_DATASOURCE_USERNAME: ${DOCUMENT_API_DATASOURCE_USERNAME}
      SPRING_DATASOURCE_PASSWORD: ${DOCUMENT_API_DATASOURCE_PASSWORD}
    depends_on:
      - postgres

volumes:
  document-api-postgres-data:
```

## document-api Dockerfile

The Dockerfile is located inside the `document-api` microservice:

```text
document-api/Dockerfile
```

Current Dockerfile:

```dockerfile
FROM gradle:9-jdk25 AS build

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./
COPY src ./src

RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

## How to Run

From the project root:

```bash
docker compose up --build
```

To run in detached mode:

```bash
docker compose up --build -d
```

Check running containers:

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
docker logs document-api
```

Or:

```bash
docker compose logs document-api
```

Stop the environment:

```bash
docker compose down
```

Stop the environment and remove orphan containers:

```bash
docker compose down --remove-orphans
```

## Test the Upload Endpoint

Create a test file:

```bash
echo "My first document" > test.txt
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
  "size": 18,
  "status": "UPLOADED",
  "createdAt": "2026-06-29T..."
}
```

## Access PostgreSQL

Enter the PostgreSQL container:

```bash
docker exec -it document-api-postgres psql -U postgres -d documentdb
```

List tables:

```sql
\dt
```

Query uploaded documents:

```sql
SELECT * FROM documents;
```

Query Flyway migration history:

```sql
SELECT * FROM flyway_schema_history;
```

Exit `psql`:

```sql
\q
```

## About `localhost:8080`

Opening the following URL in the browser returns `404 Not Found`:

```text
http://localhost:8080
```

This is expected because there is no endpoint mapped to `/` yet.

The currently available endpoint is:

```text
POST /documents/upload
```

## Local vs Magalu Cloud

The current local environment runs like this:

```text
Docker Compose
├── document-api
└── postgres
```

The target Magalu Cloud environment will evolve toward something like this:

```text
VM document-api
└── Docker
    └── document-api container

PostgreSQL Magalu Cloud

Object Storage Magalu Cloud

Kafka running in a dedicated VM or separate service
```

When using PostgreSQL from Magalu Cloud, update the datasource URL:

```env
DOCUMENT_API_DATASOURCE_URL=jdbc:postgresql://MAGALU_POSTGRES_HOST_OR_IP:5432/documentdb
```
