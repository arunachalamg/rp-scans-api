-- Run once on PostgreSQL/Supabase BEFORE restarting Spring Boot.
ALTER TABLE visits ADD COLUMN IF NOT EXISTS self BOOLEAN;
-- Preserve existing inferred values for historic visits.
UPDATE visits SET self = (referral_party_id IS NULL AND doctor_id IS NULL) WHERE self IS NULL;
ALTER TABLE visits ALTER COLUMN self SET DEFAULT FALSE;
ALTER TABLE visits ALTER COLUMN self SET NOT NULL;
