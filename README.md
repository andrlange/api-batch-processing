# Spring Boot ETL Processing Application

A Kotlin-based Spring Boot 3.5 application for ETL data processing with tenant-specific API key authentication.

This Demo showcases to create a domain specific Import Object (here PlanningDto) to validate and store it for 
processing. You can add more and different Domains by adding: Controllers, Services and DTOs.

A Scheduler ensures that a maximum number of import object is collected to forward a batch of objects (simulated via 
logging).

The delivering tenant is validated via Spring Security API Key Endpoint Protection.

## Project Structure

```
src
├── main
│   ├── kotlin
│   │  └── cool
│   │      └── cfapps
│   │          └── apibatchprocessing
│   │              ├── ApiBatchProcessingApplication.kt
│   │              ├── config
│   │              │   ├── DatabaseInitRunner.kt
│   │              │   ├── JacksonConfig.kt
│   │              │   ├── SecurityConfig.kt
│   │              │   └── security
│   │              │       ├── ApiAuthenticationToken.kt
│   │              │       ├── ApiKeyAuthenticationFilter.kt
│   │              │       ├── ApiKeyAuthenticationProvider.kt
│   │              │       ├── ApiKeyProperties.kt
│   │              │       └── SecurityContextHelper.kt
│   │              ├── controller
│   │              │   └── HealthController.kt
│   │              ├── exception
│   │              │   └── GlobalExceptionHandler.kt
│   │              └── importermodule
│   │                  ├── ImportPublicationsRepository.kt
│   │                  ├── ImportScheduler.kt
│   │                  ├── ProcessingService.kt
│   │                  ├── dto
│   │                  │   ├── ApiResponseDto.kt
│   │                  │   └── ValidationErrorResponseDto.kt
│   │                  ├── models
│   │                  │   └── ImportPublications.kt
│   │                  └── planning
│   │                      ├── PlanningController.kt
│   │                  │   ├── PlanningDto.kt
│   │                      └── PlanningService.kt
│   └── resources
│       ├── application-dev.yml
│       ├── application.yml
│       ├── appllication-prod.yml
│       ├── db
│       │    └── migration
│       │        └── V1_Create_Import_Publications.sql
│       ├── static
│       └── templates
└── test
    └── kotlin
        └── cool
            └── cfapps
                └── apibatchprocessing
                    └── ApiBatchProcessingApplicationTests.kt

```

## Getting Started

### Prerequisites

- Java 21+
- PostgreSQL 15+
- Docker (optional, for PostgreSQL)

### Database Setup

#### Option 1: Using Docker

```bash
docker compose up -d
```

alternative:
```bash
docker run --name etl-postgres \
  -e POSTGRES_DB=etl_db \
  -e POSTGRES_USER=etl_user \
  -e POSTGRES_PASSWORD=etl_password \
  -p 5432:5432 \
  -d postgres:15
```

#### Option 2: Local PostgreSQL
```sql
CREATE DATABASE etl_db;
CREATE USER etl_user WITH PASSWORD 'etl_password';
GRANT ALL PRIVILEGES ON DATABASE etl_db TO etl_user;
```

### Building and Running

```bash
# Clone and navigate to project
cd api-batch-processing

# Build the application
./gradlew build

# Run the application
./gradlew bootRun

# Or run with specific profile
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Configuration

#### API Keys
The application comes with predefined API keys for testing:

| Tenant ID | API Key |
|-----------|---------|
| tenant-001 | api-key-tenant-001-secret-12345 |
| tenant-002 | api-key-tenant-002-secret-67890 |
| tenant-003 | api-key-tenant-003-secret-abcde |

#### Environment Variables (Production)
```bash
export DATABASE_URL=jdbc:postgresql://your-db-host:5432/etl_db_prod
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
export TENANT_001_ID=your-tenant-001-id
export TENANT_001_API_KEY=your-secure-api-key-001
```

## API Endpoints

### Base URL
```
http://localhost:8080
```

### Authentication
All `/api/v1/planning/*` endpoints require the `X-API-Key` header.

### Endpoints

#### 1. Create Planning (POST)
```http
POST /api/v1/planning
Content-Type: application/json
X-API-Key: api-key-tenant-001-secret-12345

{
  "businessUnit": "Engineering",
  "resourceName": "John Doe",
  "year": 2024,
  "month": 6,
  "day": 15,
  "location": "Hamburg Office",
  "paramOne": "value1",
  "paramTwo": "value2"
}
```

#### 2. Update Planning (PUT)
```http
PUT /api/v1/planning?paramOne=value1&paramTwo=value2
Content-Type: application/json
X-API-Key: api-key-tenant-002-secret-67890

{
  "businessUnit": "Marketing",
  "resourceName": "Jane Smith",
  "year": 2024,
  "month": 7,
  "day": 20,
  "location": "Berlin Office"
}
```

#### 3. Health Check
```http
GET /health
```

## Features

### ✅ Implemented Features

1. **REST API with Validation**
    - Spring Validation for DTO fields
    - Detailed validation error responses
    - Proper HTTP status codes

2. **Tenant-based Authentication**
    - API key authentication via headers
    - Configurable tenant-to-API-key mapping
    - Security context for tenant identification

3. **Universal Storage**
    - Single `import_publications` for all DTOs
    - JSON serialization of complex objects
    - Tenant isolation and tracking

4. **Scheduled ETL Processing**
    - Runs every 30 seconds
    - Processes max 5 entries per batch
    - Automatic completion marking
    - Comprehensive logging

5. **Error Handling**
    - Global exception handler
    - Validation error responses
    - Authentication error handling

### 🔄 ETL Process Flow

1. **Data Ingestion**: API receives and validates DTOs
2. **Temporary Storage**: Valid data stored in `import_publications`
3. **Scheduled Processing**: Batch processor picks up unprocessed entries
4. **Simulation**: Processing simulated via detailed console logging
5. **Completion**: Processed entries marked with completion timestamp

### 📊 Monitoring

#### Database Queries
```sql
-- Check processing status
SELECT 
  tenant_id,
  COUNT(*) as total_entries,
  COUNT(completion_date) as processed_entries,
  COUNT(*) - COUNT(completion_date) as pending_entries
FROM import_publications 
GROUP BY tenant_id;

-- View recent entries
SELECT * FROM import_publications 
ORDER BY creation_date DESC 
LIMIT 10;
```

#### Application Metrics
- Health endpoint: `GET /actuator/health`
- Application info: `GET /actuator/info`
- Metrics: `GET /actuator/metrics`

## Testing

### Unit Tests
```bash
./gradlew test
```

### Integration Testing
Use the provided curl commands in the testing examples or tools like Postman.

### Load Testing
```bash
# Example using Apache Bench
ab -n 100 -c 10 \
  -T "application/json" \
  -H "X-API-Key: api-key-tenant-001-secret-12345" \
  -p testdata.json \
  http://localhost:8080/api/v1/planning
```

## Deployment

### Docker Deployment
```dockerfile
FROM openjdk:21-jdk-slim
COPY build/libs/etl-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Build and Deploy
```bash
./gradlew bootJar
docker build -t etl-app .
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host:5432/etl_db \
  etl-app
```

## Performance Considerations

1. **Database Indexing**: Optimized indexes for common queries
2. **Batch Processing**: Limited to 5 entries per batch to prevent overload
3. **Connection Pooling**: Spring Boot's default HikariCP configuration
4. **Async Processing**: Scheduled tasks run independently of API requests

## Security Features

1. **API Key Authentication**: Tenant-specific keys
2. **Input Validation**: Comprehensive DTO validation
3. **SQL Injection Prevention**: JPA/Hibernate parameterized queries
4. **Error Information Leakage**: Generic error messages in production

## Troubleshooting

### Common Issues

1. **Database Connection Issues**
    - Verify PostgreSQL is running
    - Check connection parameters in `application.yml`

2. **Authentication Failures**
    - Verify API key in request headers
    - Check tenant configuration in `application.yml`

3. **Validation Errors**
    - Review DTO field constraints
    - Check request payload format

### Logs
```bash
# Enable debug logging
java -jar app.jar --logging.level.cool.cfapps.apibatchprocessing=DEBUG
```

## License

This project is for demonstration purposes and follows Spring Boot's Apache 2.0 license.