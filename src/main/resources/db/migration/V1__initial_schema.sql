CREATE TABLE patients (
 id BIGSERIAL PRIMARY KEY, patient_code VARCHAR(20) UNIQUE, name VARCHAR(150) NOT NULL,
 father_husband_name VARCHAR(150), age INTEGER CHECK (age IS NULL OR age BETWEEN 0 AND 120), gender VARCHAR(20), phone VARCHAR(15), email VARCHAR(150), address TEXT, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_patients_phone ON patients(phone); CREATE INDEX idx_patients_name ON patients(name);
CREATE TABLE visits (
 id BIGSERIAL PRIMARY KEY, visit_code VARCHAR(25) UNIQUE, patient_id BIGINT NOT NULL REFERENCES patients(id), visit_date DATE NOT NULL,
 referring_doctor VARCHAR(150), referring_staff VARCHAR(150), self_referral BOOLEAN NOT NULL DEFAULT FALSE, repeat_patient BOOLEAN NOT NULL DEFAULT FALSE,
 clinical_history TEXT, gross_amount NUMERIC(12,2) NOT NULL DEFAULT 0, total_concession NUMERIC(12,2) NOT NULL DEFAULT 0, net_amount NUMERIC(12,2) NOT NULL DEFAULT 0, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_visits_patient ON visits(patient_id); CREATE INDEX idx_visits_date ON visits(visit_date);
CREATE TABLE visit_investigations (
 id BIGSERIAL PRIMARY KEY, visit_id BIGINT NOT NULL REFERENCES visits(id) ON DELETE CASCADE, modality VARCHAR(50) NOT NULL, test_name VARCHAR(200) NOT NULL,
 amount NUMERIC(12,2) NOT NULL CHECK(amount>=0), referral_code VARCHAR(50), concession BOOLEAN NOT NULL DEFAULT FALSE,
 discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK(discount_amount>=0), discount_reason VARCHAR(250), final_amount NUMERIC(12,2) NOT NULL CHECK(final_amount>=0)
);
CREATE INDEX idx_investigations_visit ON visit_investigations(visit_id); CREATE INDEX idx_investigations_referral_code ON visit_investigations(referral_code);
CREATE TABLE payments (
 id BIGSERIAL PRIMARY KEY, visit_id BIGINT NOT NULL REFERENCES visits(id) ON DELETE CASCADE, mode VARCHAR(20) NOT NULL CHECK(mode IN ('CASH','UPI','CARD')), amount NUMERIC(12,2) NOT NULL CHECK(amount>0)
);
CREATE INDEX idx_payments_visit ON payments(visit_id);
