CREATE TABLE IF NOT EXISTS audit_event_latest_events
(
    id                     BIGSERIAL,
    uuid                   UUID        NOT NULL,
    case_uuid              UUID,
    stage_uuid             UUID,
    correlation_id         TEXT        NOT NULL,
    raising_service        TEXT        NOT NULL,
    audit_payload          JSONB,
    namespace              TEXT        NOT NULL,
    audit_timestamp        TIMESTAMP   NOT NULL,
    type                   TEXT        NOT NULL,
    user_id                TEXT        NOT NULL,
    case_type              TEXT,
    deleted                BOOLEAN    NOT NULL DEFAULT FALSE,

    PRIMARY KEY (uuid),
    CONSTRAINT audit_event_latest_events_audit_timestamp_type_uuid UNIQUE (audit_timestamp, case_type, type, case_uuid),
    CONSTRAINT audit_event_latest_events_case_uuid_type UNIQUE (case_uuid, type)
);
