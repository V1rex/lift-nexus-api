# API Usage

## Local setup
docker compose up --build

## Swagger UI
http://localhost:8080/swagger-ui.html

## Typical static dispatching flow
1. Start application
2. Use seeded dev data or create warehouse data
3. Create transport orders
4. Submit dispatch job
5. Poll job status
6. Inspect assignments

## Example endpoints
POST /api/v1/dispatcher/jobs
GET /api/v1/dispatcher/jobs/{jobId}
DELETE /api/v1/dispatcher/jobs/{jobId}