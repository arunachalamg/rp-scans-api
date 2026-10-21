-- Run once in Supabase SQL Editor before deploying the Java changes.
ALTER TABLE public.visits ADD COLUMN IF NOT EXISTS doctor_id BIGINT;
ALTER TABLE public.visits DROP CONSTRAINT IF EXISTS fk_visits_doctor;
ALTER TABLE public.visits ADD CONSTRAINT fk_visits_doctor FOREIGN KEY (doctor_id) REFERENCES public.doctors(id);
CREATE INDEX IF NOT EXISTS idx_visits_doctor_id ON public.visits(doctor_id);
