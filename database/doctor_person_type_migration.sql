-- Run once in PostgreSQL (or include in a Flyway migration)
ALTER TABLE doctors ADD COLUMN IF NOT EXISTS person_type VARCHAR(10) NOT NULL DEFAULT 'DOCTOR';
ALTER TABLE doctors ADD CONSTRAINT doctors_person_type_check CHECK (person_type IN ('DOCTOR','STAFF'));
