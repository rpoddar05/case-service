-- Patient (identity) + Case (workflow) + Lab Result (facts)

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1) Patient = identity
CREATE TABLE IF NOT EXISTS patient (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    first_name varchar(64) NOT NULL,
    last_name  varchar(64) NOT NULL,
    dob        date        NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
    );

-- 2) Case = workflow episode
CREATE TABLE IF NOT EXISTS cases (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    patient_id uuid NOT NULL REFERENCES patient(id),
    status varchar(16) NOT NULL, -- OPEN / CLOSED
    opened_at timestamptz NOT NULL DEFAULT now(),
    closed_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
    );

-- 3) Lab Result = facts/events
CREATE TABLE IF NOT EXISTS lab_result (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    patient_id uuid NOT NULL REFERENCES patient(id),
    case_id uuid NULL REFERENCES cases(id),
    test_code varchar(64) NOT NULL,
    result_value varchar(128),
    result_status varchar(32) NOT NULL,
    lab_name varchar(128),
    received_at timestamptz NOT NULL DEFAULT now(),

    created_at timestamptz NOT NULL DEFAULT now()
    );