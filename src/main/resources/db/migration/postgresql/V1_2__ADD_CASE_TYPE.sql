ALTER TABLE audit_data_two
ADD COLUMN case_type text;

CREATE INDEX idx_case_type
ON audit_data_two(case_type);

UPDATE audit_data_two SET case_type = RIGHT(case_uuid::text, 2);
