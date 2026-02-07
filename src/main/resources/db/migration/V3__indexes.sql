-- Matching patient by identity
CREATE INDEX IF NOT EXISTS idx_patient_match
    ON patient (last_name, first_name, dob);

-- Optional uniqueness (for demo)
CREATE UNIQUE INDEX IF NOT EXISTS uq_patient_identity
    ON patient (lower(first_name), lower(last_name), dob);

-- Active case lookup
CREATE INDEX IF NOT EXISTS idx_cases_patient_status
    ON cases (patient_id, status);

-- Lab lookup
CREATE INDEX IF NOT EXISTS idx_lab_patient
    ON lab_result (patient_id);

CREATE INDEX IF NOT EXISTS idx_lab_case
    ON lab_result (case_id);
