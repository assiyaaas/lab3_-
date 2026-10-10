CREATE TABLE IF NOT EXISTS bank_case (
    id UUID PRIMARY KEY,
    business_key TEXT NOT NULL UNIQUE,
    status TEXT NOT NULL,
    title TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT bank_case_status_known
        CHECK (status IN ('New', 'InProgress', 'Resolved'))
);
