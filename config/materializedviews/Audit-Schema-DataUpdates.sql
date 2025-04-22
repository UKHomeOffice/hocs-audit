SET search_path TO audit;

CREATE MATERIALIZED VIEW DCU_AGGREGATED_CASES_TEMP AS
WITH CTE_Correspondents AS (
    SELECT *
    FROM (
             SELECT audit_payload::jsonb -> 'address' ->> 'address1' AS address1,
                    audit_payload::jsonb -> 'address' ->> 'address2' AS address2,
                    audit_payload::jsonb -> 'address' ->> 'address3' AS address3,
                    audit_payload::jsonb -> 'address' ->> 'postcode' AS postcode,
                    audit_payload::jsonb -> 'address' ->> 'country'  AS country,
                    audit_payload::jsonb ->> 'fullname'              AS fullname,
                    audit_payload::jsonb ->> 'email'                 AS email,
                    audit_payload::jsonb ->> 'telephone'             AS telephone,
                    audit_payload::jsonb ->> 'uuid'                  AS "correspondentUUID",
                    audit_payload::jsonb ->> 'externalKey'           AS "externalKey",
                    audit_payload::jsonb ->> 'reference'             AS "reference",
                    case_uuid::TEXT,
                    RANK() OVER (
                        PARTITION BY case_uuid, audit_payload::jsonb ->> 'uuid'
                        ORDER BY audit_timestamp DESC
                    )                                                AS MostRecentUpdate_RK,
                    audit_payload::jsonb ->> 'type'                  AS "correspondentType"
             FROM audit_event
             WHERE "type" IN ('CORRESPONDENT_CREATED', 'CORRESPONDENT_UPDATED')
               AND "case_type" IN ('a1', 'a2', 'a3')
               AND audit_payload::jsonb ->> 'uuid' NOT IN (
                 SELECT audit_payload::jsonb ->> 'uuid'
                 FROM audit_event
                 WHERE "type" = 'CORRESPONDENT_DELETED'
                   AND "case_type" IN ('a1', 'a2', 'a3')
                   AND audit_payload::jsonb ->> 'uuid' IS NOT NULL
                   AND NOT deleted
               )
               AND NOT deleted
             ORDER BY audit_timestamp DESC) ranked
    WHERE MostRecentUpdate_RK = 1
),
     CTE_CommentCounts AS (
         SELECT
             case_uuid::text,
             count(audit_timestamp) AS "commentCount"
         FROM audit_event
         WHERE "type" = ('CASE_NOTE_CREATED')
           AND case_type IN ('a1', 'a2', 'a3')
           AND case_uuid IS NOT NULL
           AND NOT deleted
         GROUP BY case_uuid
     )
SELECT grouped.*,
       COALESCE(commentCounts."commentCount", 0)                             AS "commentCount",
       NULLIF(pc.address1, '')                                               AS "primaryCorrAddress1",
       NULLIF(pc.address2, '')                                               AS "primaryCorrAddress2",
       NULLIF(pc.address3, '')                                               AS "primaryCorrAddress3",
       NULLIF(pc.country, '')                                                AS "primaryCorrCountry",
       NULLIF(pc.email, '')                                                  AS "primaryCorrEmail",
       NULLIF(pc.fullname, '')                                               AS "primaryCorrFullname",
       NULLIF(pc.postcode, '')                                               AS "primaryCorrPostcode",
       NULLIF(pc.telephone, '')                                              AS "primaryCorrTelephone",
       NULLIF(sc.address1, '')                                               AS "secondCorrAddress1",
       NULLIF(sc.address2, '')                                               AS "secondCorrAddress2",
       NULLIF(sc.address3, '')                                               AS "secondCorrAddress3",
       NULLIF(sc.country, '')                                                AS "secondCorrCountry",
       NULLIF(sc.email, '')                                                  AS "secondCorrEmail",
       NULLIF(sc.fullname, '')                                               AS "secondCorrFullname",
       NULLIF(sc.postcode, '')                                               AS "secondCorrPostcode",
       NULLIF(sc.reference, '')                                              AS "secondCorrReference",
       NULLIF(sc.telephone, '')                                              AS "secondCorrTelephone",
       NULLIF(sc."correspondentType", '')                                    AS "secondCorrType",
       CASE WHEN pc."correspondentType" = 'MEMBER' THEN pc."externalKey" END AS "mpRef",
       CASE WHEN pc."correspondentType" = 'MEMBER' THEN pc."fullname" END    AS "member",
       NOW()::TIMESTAMP(0)                                                   AS last_refresh
FROM (
         SELECT
             case_uuid::text as "caseUuid",
             MAX(deadline) FILTER( WHERE stageName LIKE '%_MARKUP' AND ranked.type = 'STAGE_CREATED' AND Last_Of_Stage_Event_RK = 1) AS "markupDeadline",
             MAX(audit_timestamp) FILTER( WHERE stageName LIKE '%_INITIAL_DRAFT' AND ranked.type = 'STAGE_RECREATED' AND Last_Of_Stage_Event_RK = 1) AS "reDraftStarted",
             MAX(allocatedToUUID) FILTER( WHERE Last_dataTypeChange_Of_Case_RK = 1 AND dataTypeChange = 'TEAM') AS "assignedTeam",
             MAX(allocatedToUUID) FILTER( WHERE Last_dataTypeChange_Of_Case_RK = 1 AND dataTypeChange = 'TEAM') AS "assignedUnit",
             MAX(allocatedToUUID) FILTER( WHERE Last_dataTypeChange_Of_Case_RK = 1 AND dataTypeChange = 'USER') AS "assignedUser",
             MAX(reference) FILTER( WHERE dataTypeChange = 'CASE_DATA' AND Last_dataTypeChange_Of_Case_RK = 1) AS "reference",
             MAX(caseDeadline) FILTER( WHERE dataTypeChange = 'CASE_DATA' AND Last_dataTypeChange_Of_Case_RK = 1) AS "caseDeadline",
             COALESCE(MAX('OGD') FILTER( WHERE "transferConfirmation" = 'ACCEPT'), MAX('NRN') FILTER( WHERE "noReplyNeededConfirmation" = 'ACCEPT'), MAX('Case Completed') FILTER( WHERE ranked.type = 'CASE_COMPLETED' AND Last_Of_Event_RK = 1), 'In Progress') AS "caseStatus",
             MAX(stageName) FILTER( WHERE Last_Stage_Event_Of_Case_RK = 1) AS "currentStage",
             MAX(originalChannel) FILTER( WHERE ranked.type = 'CASE_UPDATED' AND Last_Of_Event_RK = 1) AS "originalChannel",
             MAX(audit_timestamp) FILTER( WHERE ranked.type = 'CASE_COMPLETED' AND Last_Of_Event_RK = 1) AS "caseCompleted",
             MAX(payloadType) FILTER( WHERE ranked.type = 'CASE_CREATED' AND Last_Of_Event_RK = 1) AS "caseType",
             MAX(audit_timestamp) FILTER( WHERE ranked.type = 'CASE_CREATED') AS "caseCreated",
             MAX(dateOfCorrespondence) FILTER( WHERE Last_dataTypeChange_Of_Case_RK = 1 AND dataTypeChange = 'CASE_DATA') AS "dateOfCorrespondence",
             MAX(dateReceived) FILTER( WHERE Last_dataTypeChange_Of_Case_RK = 1 AND dataTypeChange = 'CASE_DATA') AS "dateReceived",
             MAX(audit_timestamp) FILTER( WHERE stageName LIKE '%_MINISTER_SIGN_OFF' AND ranked.type = 'STAGE_CREATED' AND Last_Of_Stage_Event_RK = 1) AS "ministerSignOffStarted",
             MAX(audit_timestamp) FILTER( WHERE stageName LIKE '%_DISPATCH' AND ranked.type = 'STAGE_ALLOCATED_TO_USER' AND Last_Of_Stage_Event_RK = 1) AS "dispatchAllocatedToUser",
             MAX(deadline) FILTER( WHERE stageName LIKE '%_DISPATCH' AND ranked.type = 'STAGE_CREATED' AND Last_Of_Stage_Event_RK = 1) AS "dispatchDeadline",
             MAX(deadline) FILTER( WHERE stageName LIKE '%_INITIAL_DRAFT' AND ranked.type = 'STAGE_CREATED' AND Last_Of_Stage_Event_RK = 1) AS "initialDraftDeadline",
             MAX(audit_timestamp) FILTER( WHERE stageName LIKE '%_INITIAL_DRAFT' AND ranked.type = 'STAGE_ALLOCATED_TO_USER' AND Last_Of_Stage_Event_RK = 1) AS "initialDraftAllocatedToUser",
             MAX(audit_timestamp) FILTER( WHERE stageName LIKE '%_MINISTER_SIGN_OFF' AND dataTypeChange = 'TEAM' AND Last_dataTypeChange_Of_Stage_RK = 1 AND allocatedToUUID = '3d2c7893-92c5-4347-804a-8826f06f0c9d') AS "homeSecSignOffStarted",
             COALESCE(MAX(true::varchar) FILTER( WHERE dataTypeChange = 'CASE_DATA' AND Last_dataTypeChange_Of_Case_RK = 1 AND privateOfficeTeamUUID = '3d2c7893-92c5-4347-804a-8826f06f0c9d'), false::varchar) AS "homeSecSignOff",
             MAX(audit_timestamp) FILTER( WHERE stageName LIKE '%_PRIVATE_OFFICE' AND dataTypeChange = 'TEAM' AND Last_dataTypeChange_Of_Stage_RK = 1 AND allocatedToUUID = '3d2c7893-92c5-4347-804a-8826f06f0c9d') AS "homeSecPrivateOfficeApprovalStarted",
             MAX(audit_timestamp) FILTER( WHERE stageName LIKE '%_MARKUP' AND ranked.type = 'STAGE_CREATED' AND Last_Of_Stage_Event_RK = 1) AS "markupStarted",
             MAX(markupDecision) FILTER( WHERE Last_dataTypeChange_Of_Case_RK = 1 AND dataTypeChange = 'CASE_DATA') AS "markupDecision",
             MAX(privateOfficeTeamUUID) FILTER( WHERE dataTypeChange = 'CASE_DATA' AND Last_dataTypeChange_Of_Case_RK = 1) AS "privateOfficeTeam",
             MAX(draftingTeamUUID) FILTER( WHERE dataTypeChange = 'CASE_DATA' AND Last_dataTypeChange_Of_Case_RK = 1) AS "draftingTeam",
             MAX(primaryTopic) FILTER( WHERE dataTypeChange = 'CASE_DATA' AND Last_dataTypeChange_Of_Case_RK = 1) AS "primaryTopic",
             MAX(draftingTeamUUID) FILTER( WHERE dataTypeChange = 'CASE_DATA' AND Last_dataTypeChange_Of_Case_RK = 1) AS "draftingUnit",
             MAX(audit_timestamp) FILTER( WHERE Last_event_Of_Case_RK = 1) AS "lastModified",
             MAX(allocatedToUUID) FILTER( WHERE stageName LIKE '%_INITIAL_DRAFT' AND ranked.type = 'STAGE_CREATED' AND First_Of_Stage_Event_RK = 1) AS "originalDraftTeam",
             MAX(allocatedToUUID) FILTER( WHERE stageName LIKE '%_INITIAL_DRAFT' AND ranked.type = 'STAGE_CREATED' AND First_Of_Stage_Event_RK = 1) AS "originalDraftUnit",
             MAX(allocatedToUUID) FILTER( WHERE stageName LIKE '%_INITIAL_DRAFT' AND ranked.type = 'STAGE_ALLOCATED_TO_USER' AND First_Of_Stage_Event_RK = 1) AS "originalDraftUser",
             MAX(audit_timestamp) FILTER( WHERE Last_dataTypeChange_Of_Case_RK = 1 AND dataTypeChange = 'USER') AS "assignedUserUpdated",
             MAX(deadline) FILTER( WHERE stageName LIKE '%_PRIVATE_OFFICE' AND ranked.type = 'STAGE_CREATED' AND Last_Of_Stage_Event_RK = 1) AS "privateOfficeDeadline",
             MAX(audit_timestamp) FILTER( WHERE stageName LIKE '%_PRIVATE_OFFICE' AND ranked.type = 'STAGE_CREATED' AND First_Of_Stage_Event_RK = 1) AS "privateOfficeStarted",
             MAX(audit_timestamp) FILTER( WHERE stageName LIKE '%_PRIVATE_OFFICE' AND ranked.type = 'STAGE_RECREATED' AND Last_Of_Stage_Event_RK = 1) AS "privateOfficeLatest",
             MAX(audit_timestamp) FILTER( WHERE stageName LIKE '%_QA_RESPONSE' AND ranked.type = 'STAGE_CREATED' AND Last_Of_Stage_Event_RK = 1) AS "qaResponseStarted",
             MAX(audit_timestamp) FILTER( WHERE stageName LIKE '%_QA_RESPONSE' AND ranked.type = 'STAGE_ALLOCATED_TO_USER' AND Last_Of_Stage_Event_RK = 1) AS "qaResponseAllocatedToUser",
             MAX(audit_timestamp) FILTER( WHERE Last_Stage_Event_Of_Case_RK = 1) AS "latestStageChange",
             MAX(audit_timestamp) FILTER( WHERE Last_dataTypeChange_Of_Case_RK = 1 AND dataTypeChange = 'CASE_DATA') AS "latestDataChange",
             MAX(user_id) FILTER( WHERE Last_event_Of_Case_RK = 1) AS "lastModifiedBy",
             MAX("ogdDept") FILTER( WHERE "transferConfirmation" = 'ACCEPT') AS "ogdDept",
             MAX("copyNumberTen") FILTER( WHERE Last_dataTypeChange_Of_Case_RK = 1 AND dataTypeChange = 'CASE_DATA') AS "copyNumberTen",
             COALESCE(MAX(true::varchar) FILTER( WHERE dataTypeChange = 'CASE_DATA' AND Last_dataTypeChange_Of_Case_RK = 1 AND privateOfficeTeamUUID = '3d2c7893-92c5-4347-804a-8826f06f0c9d' AND "ministerSignOffDecision" = 'ACCEPT'), false::varchar) AS "homeSecSignedOff",
             COALESCE(MAX(true::varchar) FILTER( WHERE dataTypeChange = 'CASE_DATA' AND Last_dataTypeChange_Of_Case_RK = 1 AND privateOfficeTeamUUID = '5311138e-33bc-434d-9bce-a933a51a3146' AND "ministerSignOffDecision" = 'ACCEPT'), false::varchar) AS "lordsMinisterSignedOff",
             MAX("primaryCorrespondentUuid") AS "primaryCorrespondentUuid",
             MAX(defaultPolicyTeamUUID) FILTER( WHERE dataTypeChange = 'CASE_DATA' AND Last_dataTypeChange_Of_Case_RK = 1) AS "defaultPolicyTeamUUID",
             MAX(defaultPolicyTeamUUID) FILTER( WHERE dataTypeChange = 'CASE_DATA' AND Last_dataTypeChange_Of_Case_RK = 1) AS "defaultPolicyTeamUnit",
             MAX(draftCount) FILTER( WHERE dataTypeChange = 'CASE_DATA' AND Last_dataTypeChange_Of_Case_RK = 1) AS "draftCount",
             MAX(qaOnlineCount) FILTER( WHERE dataTypeChange = 'CASE_DATA' AND Last_dataTypeChange_Of_Case_RK = 1) AS "qaOnlineCount",
             MAX(homeSecInterest) FILTER( WHERE dataTypeChange = 'CASE_DATA' AND Last_dataTypeChange_Of_Case_RK = 1) AS "homeSecInterest",
             MAX(homeSecReply) FILTER( WHERE dataTypeChange = 'CASE_DATA' AND Last_dataTypeChange_Of_Case_RK = 1) AS "homeSecReply",
             MAX(draftingTeamUnitHistoricName) FILTER( WHERE dataTypeChange = 'CASE_DATA' AND Last_dataTypeChange_Of_Case_RK = 1) AS "draftingTeamUnitHistoricName", -- add historic unit name fields
             MAX(poTeamUnitHistoricName) FILTER( WHERE dataTypeChange = 'CASE_DATA' AND Last_dataTypeChange_Of_Case_RK = 1) AS "poTeamUnitHistoricName",
             MAX(overrideDraftingTeamUnitHistoricName) FILTER( WHERE dataTypeChange = 'CASE_DATA' AND Last_dataTypeChange_Of_Case_RK = 1) AS "overrideDraftingTeamUnitHistoricName",
             MAX(overridePOTeamUnitHistoricName) FILTER( WHERE dataTypeChange = 'CASE_DATA' AND Last_dataTypeChange_Of_Case_RK = 1) AS "overridePOTeamUnitHistoricName",
             MAX(privateOfficeOverridePOTeamUnitHistoricName) FILTER( WHERE dataTypeChange = 'CASE_DATA' AND Last_dataTypeChange_Of_Case_RK = 1) AS "privateOfficeOverridePOTeamUnitHistoricName",
             MAX(defaultPolicyTeamUnitHistoricName) FILTER( WHERE dataTypeChange = 'CASE_DATA' AND Last_dataTypeChange_Of_Case_RK = 1) AS "defaultPolicyTeamUnitHistoricName"
         FROM (
                  SELECT
                      to_char(audit_timestamp, 'YYYY-MM-DD"T"HH24:MI:SS.US') AS audit_timestamp,
                      ad.case_uuid,
                      dataTypeChange,
                      "type",
                      user_id,
                      RANK() OVER (
                          PARTITION BY ad.case_uuid, stage_uuid, ad.type
                          ORDER BY ad.audit_timestamp DESC, ad.id DESC
                          ) AS Last_Of_Stage_Event_RK,
                      RANK() OVER (
                          PARTITION BY ad.case_uuid, stage_uuid, ad.type
                          ORDER BY ad.audit_timestamp ASC, ad.id DESC
                          ) AS First_Of_Stage_Event_RK,
                      RANK() OVER (
                          PARTITION BY ad.case_uuid, ad.type
                          ORDER BY ad.audit_timestamp DESC, ad.id DESC
                          ) AS Last_Of_Event_RK,
                      RANK() OVER (
                          PARTITION BY ad.case_uuid
                          ORDER BY CASE WHEN audit_payload::jsonb->>'stage' IS NULL THEN 0 ELSE 1 END DESC, ad.audit_timestamp DESC, ad.id DESC
                          ) AS Last_Stage_Event_Of_Case_RK,
                      RANK() OVER (
                          PARTITION BY ad.case_uuid
                          ORDER BY ad.audit_timestamp DESC, ad.id DESC
                          ) AS Last_Event_Of_Case_RK,
                      RANK() OVER (
                          PARTITION BY ad.case_uuid, dataTypeChange
                          ORDER BY ad.audit_timestamp DESC, ad.id DESC
                          ) AS Last_dataTypeChange_Of_Case_RK,
                      RANK() OVER (
                          PARTITION BY ad.case_uuid, stage_uuid, dataTypeChange
                          ORDER BY ad.audit_timestamp DESC, ad.id DESC
                          ) AS Last_dataTypeChange_Of_Stage_RK,
                      audit_payload::jsonb->>'stage' AS stageName,
                      audit_payload::jsonb->>'allocatedToUUID' AS allocatedToUUID,
                      audit_payload::jsonb->>'deadline' AS deadline,
                      audit_payload::jsonb->>'reference' AS reference,
                      audit_payload::jsonb->>'caseDeadline' AS caseDeadline,
                      audit_payload::jsonb->'data'->>'OriginalChannel' AS originalChannel,
                      audit_payload::jsonb->>'type' AS payloadType,
                      audit_payload::jsonb->'data'->>'DateOfCorrespondence' AS dateOfCorrespondence,
                      audit_payload::jsonb->'data'->>'DateReceived' AS dateReceived,
                      audit_payload::jsonb->'data'->>'MarkupDecision' AS markupDecision,
                      COALESCE(NULLIF(audit_payload::jsonb->'data'->>'OverrideDraftingTeamUUID', ''), NULLIF(audit_payload::jsonb->'data'->>'DraftingTeamUUID', '')) AS draftingTeamUUID,
                      COALESCE(NULLIF(audit_payload::jsonb->'data'->>'PrivateOfficeOverridePOTeamUUID', ''), NULLIF(audit_payload::jsonb->'data'->>'OverridePOTeamUUID', ''), NULLIF(audit_payload::jsonb->'data'->>'POTeamUUID', '')) AS privateOfficeTeamUUID,
                      audit_payload::jsonb->>'primaryTopic' AS primaryTopic,
                      audit_payload::jsonb->'data'->>'NoReplyNeededConfirmation' AS "noReplyNeededConfirmation",
                      audit_payload::jsonb->'data'->>'TransferConfirmation' AS "transferConfirmation",
                      audit_payload::jsonb->'data'->>'OGDDept' AS "ogdDept",
                      LOWER(audit_payload::jsonb->'data'->>'CopyNumberTen') AS "copyNumberTen",
                      audit_payload::jsonb->'data'->>'MinisterSignOffDecision' AS "ministerSignOffDecision",
                      audit_payload::jsonb->>'primaryCorrespondent' as "primaryCorrespondentUuid",
                      audit_payload::jsonb->'data'->>'DefaultPolicyTeamUUID' AS defaultPolicyTeamUuid,
                      audit_payload::jsonb->'data'->>'DraftCount' AS draftCount,
                      audit_payload::jsonb->'data'->>'QAOnlineCount' AS qaOnlineCount,
                      audit_payload::jsonb->'data'->>'HomeSecInterest' AS homeSecInterest,
                      audit_payload::jsonb->'data'->>'HomeSecReply' AS homeSecReply,
                      audit_payload::jsonb->'data'->>'DraftingTeamUnitHistoricName' AS draftingTeamUnitHistoricName, -- add historic unit name fields
                      audit_payload::jsonb->'data'->>'POTeamUnitHistoricName' AS poTeamUnitHistoricName,
                      audit_payload::jsonb->'data'->>'OverrideDraftingTeamUnitHistoricName' AS overrideDraftingTeamUnitHistoricName,
                      audit_payload::jsonb->'data'->>'OverridePOTeamUnitHistoricName' AS overridePOTeamUnitHistoricName,
                      audit_payload::jsonb->'data'->>'PrivateOfficeOverridePOTeamUnitHistoricName' AS privateOfficeOverridePOTeamUnitHistoricName,
                      audit_payload::jsonb->'data'->>'DefaultPolicyTeamUnitHistoricName' AS defaultPolicyTeamUnitHistoricName
                  FROM (
                           SELECT
                               audit_payload,
                               audit_timestamp,
                               case_uuid,
                               "id",
                               stage_uuid,
                               "type",
                               user_id,
                               CASE WHEN "type" IN ('CASE_CREATED', 'CASE_UPDATED') THEN 'CASE_DATA'
                                    WHEN "type" IN ('STAGE_ALLOCATED_TO_USER', 'STAGE_UNALLOCATED_FROM_USER') THEN 'USER'
                                    WHEN "type" IN ('STAGE_CREATED', 'STAGE_ALLOCATED_TO_TEAM') THEN 'TEAM'
                                   END AS dataTypeChange
                           FROM audit_event
                           WHERE case_type IN ('a1', 'a2', 'a3')
                             AND "type" IN ('CASE_COMPLETED',
                                            'CASE_CREATED',
                                            'CASE_UPDATED',
                                            'STAGE_ALLOCATED_TO_TEAM',
                                            'STAGE_ALLOCATED_TO_USER',
                                            'STAGE_COMPLETED',
                                            'STAGE_CREATED',
                                            'STAGE_RECREATED',
                                            'STAGE_UNALLOCATED_FROM_USER')
                             AND NOT deleted
                       ) ad
                  ORDER BY audit_timestamp DESC
              ) ranked
         GROUP BY case_uuid
     ) grouped
         LEFT OUTER JOIN CTE_Correspondents pc
                         ON pc."correspondentUUID" = grouped."primaryCorrespondentUuid"
         LEFT OUTER JOIN LATERAL (
    SELECT * FROM
        CTE_Correspondents cc
    WHERE cc.case_uuid = grouped."caseUuid"
      AND cc."correspondentUUID" <> grouped."primaryCorrespondentUuid"
    LIMIT 1
    ) sc
                         ON sc.case_uuid = grouped."caseUuid"
                             AND sc."correspondentUUID" <> grouped."primaryCorrespondentUuid"
         LEFT OUTER JOIN CTE_CommentCounts commentCounts
                         ON commentCounts.case_uuid = grouped."caseUuid"
WITH NO DATA;

CREATE UNIQUE INDEX idx_dcu_aggregated_cases_temp_case_uuid ON DCU_AGGREGATED_CASES_TEMP("caseUuid");

CREATE INDEX idx_dcu_aggregated_cases_temp_case_completed ON DCU_AGGREGATED_CASES_TEMP("caseCompleted");
CREATE INDEX idx_dcu_aggregated_cases_temp_case_created ON DCU_AGGREGATED_CASES_TEMP("caseCreated");
CREATE INDEX idx_dcu_aggregated_cases_temp_case_deadline ON DCU_AGGREGATED_CASES_TEMP("caseDeadline");
CREATE INDEX idx_dcu_aggregated_cases_temp_case_status ON DCU_AGGREGATED_CASES_TEMP("caseStatus");
CREATE INDEX idx_dcu_aggregated_cases_temp_case_type ON DCU_AGGREGATED_CASES_TEMP("caseType");
CREATE INDEX idx_dcu_aggregated_cases_temp_last_modified ON DCU_AGGREGATED_CASES_TEMP("lastModified");
CREATE INDEX idx_dcu_aggregated_cases_temp_latest_data_change ON DCU_AGGREGATED_CASES_TEMP("latestDataChange");

REFRESH MATERIALIZED VIEW DCU_AGGREGATED_CASES_TEMP;

DROP MATERIALIZED VIEW IF EXISTS DCU_AGGREGATED_CASES CASCADE;

ALTER TABLE DCU_AGGREGATED_CASES_TEMP RENAME TO DCU_AGGREGATED_CASES;

ALTER INDEX idx_dcu_aggregated_cases_temp_case_uuid RENAME TO idx_dcu_aggregated_cases_case_uuid;

ALTER INDEX idx_dcu_aggregated_cases_temp_case_completed RENAME TO idx_dcu_aggregated_cases_case_completed;
ALTER INDEX idx_dcu_aggregated_cases_temp_case_created RENAME TO idx_dcu_aggregated_cases_case_created;
ALTER INDEX idx_dcu_aggregated_cases_temp_case_deadline RENAME TO idx_dcu_aggregated_cases_case_deadline;
ALTER INDEX idx_dcu_aggregated_cases_temp_case_status RENAME TO idx_dcu_aggregated_cases_case_status;
ALTER INDEX idx_dcu_aggregated_cases_temp_case_type RENAME TO idx_dcu_aggregated_cases_case_type;
ALTER INDEX idx_dcu_aggregated_cases_temp_last_modified RENAME TO idx_dcu_aggregated_cases_last_modified;
ALTER INDEX idx_dcu_aggregated_cases_temp_latest_data_change RENAME TO idx_dcu_aggregated_cases_latest_data_change;

DROP VIEW IF EXISTS DCU_PRAU_WORKFLOW;

CREATE VIEW DCU_PRAU_WORKFLOW AS
SELECT
    "assignedTeam"
     ,"assignedUnit"
     ,"assignedUser"
     ,"assignedUserUpdated"
     ,"caseCompleted"
     ,"caseCreated"
     ,"caseDeadline"
     ,"caseStatus"
     ,"caseType"
     ,"currentStage"
     ,"dateOfCorrespondence"
     ,"dateReceived"
     ,"dispatchAllocatedToUser"
     ,"dispatchDeadline"
     ,"draftingTeam"
     ,"draftingUnit"
     ,"homeSecPrivateOfficeApprovalStarted"
     ,"homeSecSignOff"
     ,"homeSecSignOffStarted"
     ,"initialDraftAllocatedToUser"
     ,"initialDraftDeadline"
     ,"lastModified"
     ,"latestDataChange"
     ,"latestStageChange"
     ,"markupDeadline"
     ,"markupDecision"
     ,"markupStarted"
     ,"member"
     ,"ministerSignOffStarted"
     ,"originalChannel"
     ,"originalDraftTeam"
     ,"originalDraftUnit"
     ,"originalDraftUser"
     ,"primaryTopic"
     ,"privateOfficeDeadline"
     ,"privateOfficeStarted"
     ,"privateOfficeLatest"
     ,"privateOfficeTeam"
     ,"qaResponseAllocatedToUser"
     ,"qaResponseStarted"
     ,"draftCount"
     ,"reDraftStarted"
     ,"reference"
     ,"defaultPolicyTeamUUID"
     ,"defaultPolicyTeamUnit"
     ,"qaOnlineCount"
     ,"homeSecInterest"
     ,"homeSecReply"
     ,"draftingTeamUnitHistoricName"
     ,"poTeamUnitHistoricName"
     ,"overrideDraftingTeamUnitHistoricName"
     ,"overridePOTeamUnitHistoricName"
     ,"privateOfficeOverridePOTeamUnitHistoricName"
     ,"defaultPolicyTeamUnitHistoricName"
     ,"last_refresh"
FROM DCU_AGGREGATED_CASES;

DROP VIEW IF EXISTS DCU_BUSINESS;

CREATE VIEW DCU_BUSINESS AS
SELECT
    "assignedTeam"
     ,"assignedUnit"
     ,"assignedUser"
     ,"assignedUserUpdated"
     ,"caseCompleted"
     ,"caseCreated"
     ,"caseDeadline"
     ,"caseStatus"
     ,"caseType"
     ,"caseUuid"
     ,"commentCount"
     ,"copyNumberTen"
     ,"currentStage"
     ,"dateOfCorrespondence"
     ,"dateReceived"
     ,"dispatchAllocatedToUser"
     ,"dispatchDeadline"
     ,"draftingTeam"
     ,"draftingUnit"
     ,"homeSecPrivateOfficeApprovalStarted"
     ,"homeSecSignedOff"
     ,"homeSecSignOff"
     ,"homeSecSignOffStarted"
     ,"initialDraftAllocatedToUser"
     ,"initialDraftDeadline"
     ,"lastModified"
     ,"lastModifiedBy"
     ,"latestDataChange"
     ,"latestStageChange"
     ,"lordsMinisterSignedOff"
     ,"markupDeadline"
     ,"markupDecision"
     ,"markupStarted"
     ,"member"
     ,"ministerSignOffStarted"
     ,"mpRef"
     ,"ogdDept"
     ,"originalChannel"
     ,"originalDraftTeam"
     ,"originalDraftUnit"
     ,"originalDraftUser"
     ,"primaryCorrAddress1"
     ,"primaryCorrAddress2"
     ,"primaryCorrAddress3"
     ,"primaryCorrCountry"
     ,"primaryCorrEmail"
     ,"primaryCorrFullname"
     ,"primaryCorrPostcode"
     ,"primaryCorrTelephone"
     ,"primaryTopic"
     ,"privateOfficeDeadline"
     ,"privateOfficeStarted"
     ,"privateOfficeLatest"
     ,"privateOfficeTeam"
     ,"qaResponseAllocatedToUser"
     ,"qaResponseStarted"
     ,"draftCount"
     ,"reDraftStarted"
     ,"reference"
     ,"secondCorrAddress1"
     ,"secondCorrAddress2"
     ,"secondCorrAddress3"
     ,"secondCorrCountry"
     ,"secondCorrEmail"
     ,"secondCorrFullname"
     ,"secondCorrPostcode"
     ,"secondCorrTelephone"
     ,"secondCorrType"
     ,"defaultPolicyTeamUUID"
     ,"defaultPolicyTeamUnit"
     ,"qaOnlineCount"
     ,"homeSecInterest"
     ,"homeSecReply"
     ,"draftingTeamUnitHistoricName"
     ,"poTeamUnitHistoricName"
     ,"overrideDraftingTeamUnitHistoricName"
     ,"overridePOTeamUnitHistoricName"
     ,"privateOfficeOverridePOTeamUnitHistoricName"
     ,"defaultPolicyTeamUnitHistoricName"
     ,"last_refresh"
FROM DCU_AGGREGATED_CASES;

-- REFRESH MATERIALIZED VIEW DCU_AGGREGATED_CASES;
