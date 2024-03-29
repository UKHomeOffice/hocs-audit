
--- monthly
CREATE TABLE audit_event_2026_1 PARTITION OF audit_event
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2026_1_viewed PARTITION OF audit_event_2026_1
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2026_1_default PARTITION OF audit_event_2026_1
    DEFAULT;

---
CREATE TABLE audit_event_2026_2 PARTITION OF audit_event
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2026_2_viewed PARTITION OF audit_event_2026_2
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2026_2_default PARTITION OF audit_event_2026_2
    DEFAULT;

---
CREATE TABLE audit_event_2026_3 PARTITION OF audit_event
    FOR VALUES FROM ('2026-03-01') TO ('2026-04-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2026_3_viewed PARTITION OF audit_event_2026_3
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2026_3_default PARTITION OF audit_event_2026_3
    DEFAULT;

---
CREATE TABLE audit_event_2026_4 PARTITION OF audit_event
    FOR VALUES FROM ('2026-04-01') TO ('2026-05-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2026_4_viewed PARTITION OF audit_event_2026_4
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2026_4_default PARTITION OF audit_event_2026_4
    DEFAULT;

---
CREATE TABLE audit_event_2026_5 PARTITION OF audit_event
    FOR VALUES FROM ('2026-05-01') TO ('2026-06-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2026_5_viewed PARTITION OF audit_event_2026_5
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2026_5_default PARTITION OF audit_event_2026_5
    DEFAULT;

---
CREATE TABLE audit_event_2026_6 PARTITION OF audit_event
    FOR VALUES FROM ('2026-06-01') TO ('2026-07-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2026_6_viewed PARTITION OF audit_event_2026_6
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2026_6_default PARTITION OF audit_event_2026_6
    DEFAULT;

---

CREATE TABLE audit_event_2026_7 PARTITION OF audit_event
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2026_7_viewed PARTITION OF audit_event_2026_7
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2026_7_default PARTITION OF audit_event_2026_7
    DEFAULT;

---

CREATE TABLE audit_event_2026_8 PARTITION OF audit_event
    FOR VALUES FROM ('2026-08-01') TO ('2026-09-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2026_8_viewed PARTITION OF audit_event_2026_8
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2026_8_default PARTITION OF audit_event_2026_8
    DEFAULT;

---

CREATE TABLE audit_event_2026_9 PARTITION OF audit_event
    FOR VALUES FROM ('2026-09-01') TO ('2026-10-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2026_9_viewed PARTITION OF audit_event_2026_9
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2026_9_default PARTITION OF audit_event_2026_9
    DEFAULT;

---

CREATE TABLE audit_event_2026_10 PARTITION OF audit_event
    FOR VALUES FROM ('2026-10-01') TO ('2026-11-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2026_10_viewed PARTITION OF audit_event_2026_10
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2026_10_default PARTITION OF audit_event_2026_10
    DEFAULT;

---

CREATE TABLE audit_event_2026_11 PARTITION OF audit_event
    FOR VALUES FROM ('2026-11-01') TO ('2026-12-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2026_11_viewed PARTITION OF audit_event_2026_11
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2026_11_default PARTITION OF audit_event_2026_11
    DEFAULT;

---

CREATE TABLE audit_event_2026_12 PARTITION OF audit_event
    FOR VALUES FROM ('2026-12-01') TO ('2027-01-01')
    PARTITION BY LIST(type);

CREATE TABLE audit_event_2026_12_viewed PARTITION OF audit_event_2026_12
    FOR VALUES IN ('CASE_SUMMARY_VIEWED','CASE_VIEWED','SOMU_ITEMS_VIEWED','SOMU_ITEM_VIEWED','STANDARD_LINE_VIEWED','TEMPLATE_VIEWED');

CREATE TABLE audit_event_2026_12_default PARTITION OF audit_event_2026_12
    DEFAULT;
