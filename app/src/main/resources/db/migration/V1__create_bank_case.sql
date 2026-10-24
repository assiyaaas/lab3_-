CREATE TABLE bank_case (
                           id            uuid PRIMARY KEY,
                           business_key  text NOT NULL UNIQUE,
                           status        text NOT NULL,
                           title         text NOT NULL,
                           created_at    timestamptz NOT NULL DEFAULT now(),
                           CONSTRAINT bank_case_status_known
                               CHECK (status IN ('New', 'InProgress', 'Resolved'))
);