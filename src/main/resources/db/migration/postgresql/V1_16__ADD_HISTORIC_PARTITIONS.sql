CREATE TABLE audit_event_2016 PARTITION OF audit_event
    FOR VALUES FROM ('2016-01-01') TO ('2018-01-01');

CREATE TABLE audit_event_2014 PARTITION OF audit_event
    FOR VALUES FROM ('2014-01-01') TO ('2016-01-01');

CREATE TABLE audit_event_2012 PARTITION OF audit_event
    FOR VALUES FROM ('2012-01-01') TO ('2014-01-01');

CREATE TABLE audit_event_2010 PARTITION OF audit_event
    FOR VALUES FROM ('2010-01-01') TO ('2012-01-01');

CREATE TABLE audit_event_2008 PARTITION OF audit_event
    FOR VALUES FROM ('2008-01-01') TO ('2010-01-01');
