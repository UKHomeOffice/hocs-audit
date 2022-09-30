{{/*
Expand the name of the chart.
*/}}
{{- define "hocs-app.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "hocs-app.selectorLabels" -}}
name: {{ include "hocs-app.name" . }}
role: {{ tpl .Values.app.selectorRole . }}
version: {{.Values.version}}
{{- end }}

{{/*
Security context
*/}}
{{- define "hocs-app.securityContext" -}}
runAsNonRoot: true
capabilities:
  drop:
    - SETUID
    - SETGID
{{- end }}
