# RP SCANS API
Spring Boot 4.1.1 / Java 21 / Maven / PostgreSQL (Supabase).

## 1. Create database tables
Open Supabase SQL Editor and run `database/schema.sql`.

## 2. Configure connection
Use the Supabase **Session Pooler** values. Export:
```bash
export SUPABASE_DB_URL='jdbc:postgresql://YOUR_POOLER_HOST:5432/postgres?sslmode=require'
export SUPABASE_DB_USERNAME='postgres.YOUR_PROJECT_REF'
export SUPABASE_DB_PASSWORD='YOUR_PASSWORD'
```
Never commit the real database password.

## 3. Run
```bash
mvn spring-boot:run
```
Health: `GET http://localhost:8080/actuator/health`

## Main endpoints
- `GET/POST/PUT/DELETE /api/doctors`
- `GET/POST/PUT/DELETE /api/investigations`
- `GET /api/patients?query=...`, `POST /api/patients`, `GET /api/patients/{id}`
- `GET/POST/PUT /api/referrals`
- `GET /api/visits`, `POST /api/visits`, `GET /api/visits/{id}`
- `GET /api/reports/referrals/summary?from=2026-09-01&to=2026-09-30`

The uploaded React pages currently contain local sample state. Wire them to these APIs with `fetch`/Axios next.
