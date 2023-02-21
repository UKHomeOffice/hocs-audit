CREATE OR REPLACE FUNCTION upsertLatestAuditEvents() RETURNS TRIGGER AS
$BODY$
BEGIN
    INSERT INTO audit_event_latest_events
    (uuid, case_uuid, stage_uuid, correlation_id, raising_service, audit_payload, namespace, audit_timestamp, type, user_id, case_type)
    VALUES (NEW.uuid, NEW.case_uuid, NEW.stage_uuid, NEW.correlation_id, NEW.raising_service, NEW.audit_payload, NEW.namespace, NEW.audit_timestamp, NEW.type, NEW.user_id, NEW.case_type)
    ON CONFLICT (case_uuid, type) DO UPDATE
        SET uuid = EXCLUDED.uuid, stage_uuid = EXCLUDED.stage_uuid, correlation_id = EXCLUDED.correlation_id, raising_service = EXCLUDED.raising_service, audit_payload = EXCLUDED.audit_payload,
            audit_timestamp = EXCLUDED.audit_timestamp, user_id = EXCLUDED.user_id
        WHERE audit_event_latest_events.audit_timestamp < excluded.audit_timestamp;

    RETURN NEW;
END;
$BODY$
language plpgsql;

CREATE OR REPLACE FUNCTION updateLatestAuditEventsDeleted() RETURNS TRIGGER AS
$BODY$
BEGIN
    UPDATE audit_event_latest_events
    SET deleted = NEW.deleted
    WHERE uuid = NEW.uuid;

    RETURN NEW;
END;
$BODY$
language plpgsql;

CREATE TRIGGER auditEventLatestTypesTrigger
    AFTER INSERT ON audit_event
    FOR EACH ROW
    WHEN (NEW.type in ('CASE_CREATED', 'CASE_UPDATED', 'CASE_COMPLETED'))
EXECUTE PROCEDURE upsertLatestAuditEvents();

CREATE TRIGGER auditEventUpdateDeletedFlag
    AFTER UPDATE ON audit_event
    FOR EACH ROW
    WHEN (OLD.deleted <> NEW.deleted)
EXECUTE PROCEDURE updateLatestAuditEventsDeleted();
