CREATE TABLE IF NOT EXISTS case_ingest_error (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at timestamptz NOT NULL DEFAULT now(),
    topic varchar(200) NOT NULL,
    partition int NOT NULL,
    kafka_offset bigint NOT NULL,
    message_key varchar(200),
    correlation_id varchar(200),
    error_type varchar(100) NOT NULL,
    error_message text NOT NULL,
    payload text,
    status varchar(20) NOT NULL DEFAULT 'NEW' -- NEW, FIXED, REPLAYED, IGNORED
    );

CREATE INDEX IF NOT EXISTS idx_case_ingest_error_status ON case_ingest_error(status);
CREATE INDEX IF NOT EXISTS idx_case_ingest_error_created_at ON case_ingest_error(created_at);
CREATE INDEX IF NOT EXISTS idx_case_ingest_error_tp_offset ON case_ingest_error(topic, partition, kafka_offset);
