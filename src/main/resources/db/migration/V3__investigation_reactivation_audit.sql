CREATE TABLE IF NOT EXISTS investigation_status_audit (
    id BIGSERIAL PRIMARY KEY,
    visit_investigation_id BIGINT NOT NULL REFERENCES visit_investigations(id),
    visit_id BIGINT NOT NULL REFERENCES visits(id),
    action VARCHAR(20) NOT NULL CHECK (action IN ('CANCELLED', 'REACTIVATED')),
    reason VARCHAR(1000) NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_investigation_status_audit_item ON investigation_status_audit(visit_investigation_id, changed_at);
