ALTER TABLE lab_result
    ADD COLUMN IF NOT EXISTS event_id varchar(64);

-- Backfill existing rows so constraint can be added safely
UPDATE lab_result
SET event_id = id::text
WHERE event_id IS NULL;

ALTER TABLE lab_result
    ALTER COLUMN event_id SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_lab_result_event_id
    ON lab_result(event_id);
