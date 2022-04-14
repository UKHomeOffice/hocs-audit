
DROP INDEX IF EXISTS idx_audit_events_timestamp_type_case_type;

CREATE INDEX IF NOT EXISTS idx_audit_events_type_case_type ON audit_event(type, case_type) WHERE case_type IS NOT NULL;
