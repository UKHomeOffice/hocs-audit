CREATE OR REPLACE TRIGGER auditEventLatestTypesTrigger
    AFTER INSERT ON audit_event
    FOR EACH ROW
    WHEN (NEW.type in ('CASE_CREATED'))
EXECUTE PROCEDURE upsertLatestAuditEvents();
