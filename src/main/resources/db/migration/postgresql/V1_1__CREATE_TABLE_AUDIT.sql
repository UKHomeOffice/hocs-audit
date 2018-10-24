DROP TABLE IF EXISTS audit_data cascade;

CREATE TABLE IF NOT EXISTS audit_data
(
  id                     BIGSERIAL   PRIMARY KEY,
  uuid                   UUID        NOT NULL,
  correlation_id         TEXT        NOT NULL,
  raising_service        TEXT        NOT NULL,
  before                 JSONB,
  after                  JSONB,
  namespace              TEXT        NOT NULL,
  audit_timestamp        TIMESTAMP   NOT NULL,
  type                   TEXT        NOT NULL,
  user_id                TEXT        NOT NULL DEFAULT 'anon',


  CONSTRAINT audit_uuid_idempotent UNIQUE (uuid)
);
