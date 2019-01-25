DROP TABLE IF EXISTS audit_data cascade;

CREATE TABLE IF NOT EXISTS audit_data
(
  id                     BIGSERIAL   PRIMARY KEY,
  uuid                   UUID        NOT NULL,
  case_uuid              UUID,
  correlation_id         TEXT        NOT NULL,
  raising_service        TEXT        NOT NULL,
  audit_payload          JSONB,
  namespace              TEXT        NOT NULL,
  audit_timestamp        TIMESTAMP   NOT NULL,
  type                   TEXT        NOT NULL,
  user_id                TEXT        NOT NULL,


  CONSTRAINT audit_uuid_idempotent UNIQUE (uuid)
);

CREATE INDEX idx_case_uuid ON audit_data (case_uuid);
