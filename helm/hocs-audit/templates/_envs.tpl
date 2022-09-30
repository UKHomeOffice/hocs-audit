{{- define "deployment.envs" }}
- name: JAVA_OPTS
  value: '{{ tpl .Values.app.javaOpts . }}'
- name: SERVER_PORT
  value: '{{ tpl .Values.app.port . }}'
- name: SPRING_PROFILES_ACTIVE
  value: '{{ tpl .Values.app.springProfiles . }}'
- name: DB_HOST
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-audit-rds
      key: host
- name: DB_PORT
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-audit-rds
      key: port
- name: DB_NAME
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-audit-rds
      key: name
- name: DB_SCHEMA_NAME
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-audit-rds
      key: schema_name
- name: DB_USERNAME
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-audit-rds
      key: user_name
- name: DB_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-audit-rds
      key: password
- name: AUDIT_QUEUE_NAME
  value: {{ .Release.Namespace }}-audit-sqs
- name: AWS_SQS_AUDIT_URL
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-audit-sqs
      key: sqs_queue_url
- name: AWS_SQS_ACCESS_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-audit-sqs
      key: access_key_id
- name: AWS_SQS_SECRET_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-audit-sqs
      key: secret_access_key
- name: HOCS_INFO_SERVICE
  value: '{{ tpl .Values.app.infoService . }}'
- name: HOCS_CASE_SERVICE
  value: '{{ tpl .Values.app.caseService . }}'
- name: HOCS_BASICAUTH
  valueFrom:
    secretKeyRef:
      name: ui-casework-creds
      key: plaintext
{{- end -}}
