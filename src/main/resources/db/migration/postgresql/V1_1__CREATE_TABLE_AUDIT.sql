CREATE TABLE IF NOT EXISTS audit
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
    case_type              text,
    deleted                BOOLEAN    NOT NULL DEFAULT FALSE,

    PRIMARY KEY (uuid, audit_timestamp, type, case_type),
    CONSTRAINT audit_event_uuid_idempotent UNIQUE (uuid, audit_timestamp, type, case_type)
    ) PARTITION BY LIST (case_type);

CREATE TABLE audit_tenant_default PARTITION OF audit
    DEFAULT;

CREATE TABLE audit_tenant_one PARTITION OF audit
    FOR VALUES IN ('b6')
    PARTITION BY LIST(type);

    CREATE TABLE audit_tenant_one_default PARTITION OF audit_tenant_one
        DEFAULT
        PARTITION BY RANGE (audit_timestamp);

        CREATE TABLE audit_tenant_one_default_default PARTITION OF audit_tenant_one_default
            DEFAULT;

        CREATE TABLE audit_tenant_one_default_default_2022 PARTITION OF audit_tenant_one_default_default
                FOR VALUES FROM ('2022-01-01') TO ('2023-01-01');

        CREATE TABLE audit_tenant_one_default_default_2023 PARTITION OF audit_tenant_one_default_default
                FOR VALUES FROM ('2023-01-01') TO ('2024-12-01');


    CREATE TABLE audit_tenant_one_created_completed PARTITION OF audit_tenant_one
        FOR VALUES IN ('CASE_CREATED','CASE_COMPLETED')
        PARTITION BY RANGE (audit_timestamp);

        CREATE TABLE audit_tenant_one_created_completed_default PARTITION OF audit_tenant_one_created_completed
            DEFAULT;

        CREATE TABLE audit_tenant_one_created_completed_2022 PARTITION OF audit_tenant_one_created_completed
                FOR VALUES FROM ('2022-01-01') TO ('2023-01-01');

        CREATE TABLE audit_tenant_one_created_completed_2023 PARTITION OF audit_tenant_one_created_completed
                FOR VALUES FROM ('2023-01-01') TO ('2024-12-01');


    CREATE TABLE audit_tenant_one_updated PARTITION OF audit_tenant_one
        FOR VALUES IN ('CASE_UPDATED')
        PARTITION BY RANGE (audit_timestamp);

        CREATE TABLE audit_tenant_one_updated_default PARTITION OF audit_tenant_one_updated
            DEFAULT;

        CREATE TABLE audit_tenant_one_updated_2022_10 PARTITION OF audit_tenant_one_updated
                FOR VALUES FROM ('2022-10-01') TO ('2022-11-01');

        CREATE TABLE audit_tenant_one_updated_2022_11 PARTITION OF audit_tenant_one_updated
                FOR VALUES FROM ('2022-11-01') TO ('2022-12-01');

        CREATE TABLE audit_tenant_one_updated_2022_12 PARTITION OF audit_tenant_one_updated
                FOR VALUES FROM ('2022-12-01') TO ('2023-01-01');

        CREATE TABLE audit_tenant_one_updated_2023_01 PARTITION OF audit_tenant_one_updated
                FOR VALUES FROM ('2023-01-01') TO ('2023-02-01');

        CREATE TABLE audit_tenant_one_updated_2023_02 PARTITION OF audit_tenant_one_updated
                FOR VALUES FROM ('2023-02-01') TO ('2023-03-01');

        CREATE TABLE audit_tenant_one_updated_2023_03 PARTITION OF audit_tenant_one_updated
                FOR VALUES FROM ('2023-03-01') TO ('2023-03-01');


    CREATE TABLE audit_tenant_one_viewed PARTITION OF audit_tenant_one
        FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED')
        PARTITION BY RANGE (audit_timestamp);

        CREATE TABLE audit_tenant_one_viewed_default PARTITION OF audit_tenant_one_viewed
            DEFAULT;

        CREATE TABLE audit_tenant_one_viewed_2022 PARTITION OF audit_tenant_one_viewed
                FOR VALUES FROM ('2022-01-01') TO ('2023-01-01');

        CREATE TABLE audit_tenant_one_viewed_2023 PARTITION OF audit_tenant_one_viewed
                FOR VALUES FROM ('2023-01-01') TO ('2024-12-01');

---

CREATE INDEX idx_audit_type ON audit (type, audit_timestamp DESC) where case_uuid is null;

CREATE INDEX idx_audit_case_uuid ON audit (case_uuid, type, audit_timestamp DESC) where case_uuid is not null;
