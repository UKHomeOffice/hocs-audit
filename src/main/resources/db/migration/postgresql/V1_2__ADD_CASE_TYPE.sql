ALTER TABLE audit_data
ADD COLUMN case_type text;

CREATE INDEX idx_case_type
ON audit_data(case_type);

UPDATE audit_data SET case_type = RIGHT(case_uuid::text, 2);