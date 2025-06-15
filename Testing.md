# Testing the Application

## 1. Start PostgreSQL Database

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
  -d postgres:17
```

## 2. Test Endpoints

### POST Endpoint Test
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -H "X-API-Key: api-key-tenant-001-secret-12345" \
  -d '{
    "businessUnit": "Engineering",
    "resourceName": "John Doe",
    "year": 2024,
    "month": 6,
    "day": 15,
    "location": "Hamburg Office",
    "paramOne": "value1",
    "paramTwo": "value2"
  }' \
  http://localhost:8080/api/v1/planning
```

Expected Response:
```json
{
  "success": true,
  "message": "Planning data stored successfully",
  "data": {
    "id": 1
  }
}
```

### PUT Endpoint Test
```bash
curl -X PUT \
  -H "Content-Type: application/json" \
  -H "X-API-Key: api-key-tenant-002-secret-67890" \
  -d '{
    "businessUnit": "Marketing",
    "resourceName": "Jane Smith",
    "year": 2024,
    "month": 7,
    "day": 20,
    "location": "Berlin Office"
  }' \
  "http://localhost:8080/api/v1/planning?paramOne=value1&paramTwo=value2"
```

### Validation Error Test
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -H "X-API-Key: api-key-tenant-001-secret-12345" \
  -d '{
    "businessUnit": "",
    "resourceName": "John Doe",
    "year": 2019,
    "month": 13,
    "day": 32,
    "location": "",
    "paramOne": "value1",
    "paramTwo": "value2"
  }' \
  http://localhost:8080/api/v1/planning
```

Expected Response:
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "businessUnit": "Business unit cannot be blank",
    "year": "Year must be at least 2020",
    "month": "Month must be between 1 and 12",
    "day": "Day must be between 1 and 31",
    "location": "Location cannot be blank"
  }
}
```

### Invalid API Key Test
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -H "X-API-Key: invalid-key" \
  -d '{
    "businessUnit": "Engineering",
    "resourceName": "John Doe",
    "year": 2024,
    "month": 6,
    "day": 15,
    "location": "Hamburg Office",
    "paramOne": "value1",
    "paramTwo": "value2"
  }' \
  http://localhost:8080/api/v1/planning
```

Expected Response:
```json
{
  "success": false,
  "message": "Invalid API key",
  "timestamp": 1718467200000
}
```

## 3. Check Database
```sql
-- Connect to PostgreSQL and run:
SELECT * FROM import_publications ORDER BY creation_date DESC;

-- Check processing status:
SELECT 
  tenant_id,
  COUNT(*) as total_entries,
  COUNT(completion_date) as processed_entries,
  COUNT(*) - COUNT(completion_date) as pending_entries
FROM import_publications 
GROUP BY tenant_id;
```

## 4. Monitor Logs
Watch the application logs to see the scheduled ETL processing every 30 seconds:
```
2024-06-15 15:30:00.123 INFO  - Found 2 unprocessed entries to process
2024-06-15 15:30:00.124 INFO  - === ETL PROCESSING START ===
2024-06-15 15:30:00.125 INFO  - Processing 2 entries...
2024-06-15 15:30:00.126 INFO  - Processing Entry:
- ID: 1
- Tenant: tenant-001
- Type: PlanningDTO
- Created: 2024-06-15T15:29:45.123
- Data: {
  "businessUnit" : "Engineering",
  "resourceName" : "John Doe",
  ...
}
```
