CREATE TABLE IF NOT EXISTS audit_event
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

    PRIMARY KEY (uuid, audit_timestamp, type),
    CONSTRAINT audit_event_uuid_idempotent UNIQUE (uuid, audit_timestamp, type)
    ) PARTITION BY RANGE (audit_timestamp);

--- annually
CREATE TABLE audit_event_2018 PARTITION OF audit_event
    FOR VALUES FROM ('2018-01-01') TO ('2019-01-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2018_viewed PARTITION OF audit_event_2018
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2018_default PARTITION OF audit_event_2018
    DEFAULT;

--- annually
CREATE TABLE audit_event_2019 PARTITION OF audit_event
    FOR VALUES FROM ('2019-01-01') TO ('2020-01-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2019_viewed PARTITION OF audit_event_2019
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2019_default PARTITION OF audit_event_2019
    DEFAULT;

--- quarterly
CREATE TABLE audit_event_2020 PARTITION OF audit_event
    FOR VALUES FROM ('2020-01-01') TO ('2021-01-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2020_viewed PARTITION OF audit_event_2020
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2020_default PARTITION OF audit_event_2020
    DEFAULT;
---
CREATE TABLE audit_event_2021_1 PARTITION OF audit_event
    FOR VALUES FROM ('2021-01-01') TO ('2021-03-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2021_1_viewed PARTITION OF audit_event_2021_1
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2021_1_default PARTITION OF audit_event_2021_1
    DEFAULT;
---
CREATE TABLE audit_event_2021_2 PARTITION OF audit_event
    FOR VALUES FROM ('2021-03-01') TO ('2021-06-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2021_2_viewed PARTITION OF audit_event_2021_2
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2021_2_default PARTITION OF audit_event_2021_2
    DEFAULT;

---
CREATE TABLE audit_event_2021_3 PARTITION OF audit_event
    FOR VALUES FROM ('2021-06-01') TO ('2021-09-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2021_3_viewed PARTITION OF audit_event_2021_3
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2021_3_default PARTITION OF audit_event_2021_3
    DEFAULT;

---
CREATE TABLE audit_event_2021_4 PARTITION OF audit_event
    FOR VALUES FROM ('2021-09-01') TO ('2022-01-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2021_4_viewed PARTITION OF audit_event_2021_4
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2021_4_default PARTITION OF audit_event_2021_4
    DEFAULT;

--- monthly
CREATE TABLE audit_event_2022_1 PARTITION OF audit_event
    FOR VALUES FROM ('2022-01-01') TO ('2022-02-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2022_1_viewed PARTITION OF audit_event_2022_1
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2022_1_default PARTITION OF audit_event_2022_1
    DEFAULT;

---
CREATE TABLE audit_event_2022_2 PARTITION OF audit_event
    FOR VALUES FROM ('2022-02-01') TO ('2022-03-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2022_2_viewed PARTITION OF audit_event_2022_2
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2022_2_default PARTITION OF audit_event_2022_2
    DEFAULT;

---
CREATE TABLE audit_event_2022_3 PARTITION OF audit_event
    FOR VALUES FROM ('2022-03-01') TO ('2022-04-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2022_3_viewed PARTITION OF audit_event_2022_3
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2022_3_default PARTITION OF audit_event_2022_3
    DEFAULT;

---
CREATE TABLE audit_event_2022_4 PARTITION OF audit_event
    FOR VALUES FROM ('2022-04-01') TO ('2022-05-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2022_4_viewed PARTITION OF audit_event_2022_4
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2022_4_default PARTITION OF audit_event_2022_4
    DEFAULT;

---
CREATE TABLE audit_event_2022_5 PARTITION OF audit_event
    FOR VALUES FROM ('2022-05-01') TO ('2022-06-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2022_5_viewed PARTITION OF audit_event_2022_5
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2022_5_default PARTITION OF audit_event_2022_5
    DEFAULT;

---
CREATE TABLE audit_event_2022_6 PARTITION OF audit_event
    FOR VALUES FROM ('2022-06-01') TO ('2022-07-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2022_6_viewed PARTITION OF audit_event_2022_6
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2022_6_default PARTITION OF audit_event_2022_6
    DEFAULT;

---

CREATE TABLE audit_event_2022_7 PARTITION OF audit_event
    FOR VALUES FROM ('2022-07-01') TO ('2022-08-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2022_7_viewed PARTITION OF audit_event_2022_7
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2022_7_default PARTITION OF audit_event_2022_7
    DEFAULT;

---

CREATE TABLE audit_event_2022_8 PARTITION OF audit_event
    FOR VALUES FROM ('2022-08-01') TO ('2022-09-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2022_8_viewed PARTITION OF audit_event_2022_8
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2022_8_default PARTITION OF audit_event_2022_8
    DEFAULT;

---

CREATE TABLE audit_event_2022_9 PARTITION OF audit_event
    FOR VALUES FROM ('2022-09-01') TO ('2022-10-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2022_9_viewed PARTITION OF audit_event_2022_9
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2022_9_default PARTITION OF audit_event_2022_9
    DEFAULT;

---

CREATE TABLE audit_event_2022_10 PARTITION OF audit_event
    FOR VALUES FROM ('2022-10-01') TO ('2022-11-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2022_10_viewed PARTITION OF audit_event_2022_10
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2022_10_default PARTITION OF audit_event_2022_10
    DEFAULT;

---

CREATE TABLE audit_event_2022_11 PARTITION OF audit_event
    FOR VALUES FROM ('2022-11-01') TO ('2022-12-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2022_11_viewed PARTITION OF audit_event_2022_11
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2022_11_default PARTITION OF audit_event_2022_11
    DEFAULT;

---

CREATE TABLE audit_event_2022_12 PARTITION OF audit_event
    FOR VALUES FROM ('2022-12-01') TO ('2023-01-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2022_12_viewed PARTITION OF audit_event_2022_12
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2022_12_default PARTITION OF audit_event_2022_12
    DEFAULT;

---

CREATE INDEX idx_audit_events_case_uuid ON audit_event (case_uuid, type) where case_uuid is not null;
CREATE INDEX idx_audit_events_timestamp_type_case_type on audit_event (audit_timestamp DESC, type, case_type, case_uuid) where case_uuid is not null;