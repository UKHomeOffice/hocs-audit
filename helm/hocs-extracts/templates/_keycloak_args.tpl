{{- define "keycloak.args" }}
- --config=/etc/secrets/data.yml
- --discovery-url={{ .Values.keycloak.realm }}
- --openid-provider-proxy=http://hocs-outbound-proxy.{{ .Release.Namespace }}.svc.cluster.local:31290
- --enable-logging=true
- --enable-json-logging=true
- --upstream-url=http://127.0.0.1:{{ include "hocs-app.port" . }}
- --upstream-response-header-timeout={{ .Values.keycloak.timeout }}s
- --upstream-expect-continue-timeout={{ .Values.keycloak.timeout }}s
- --upstream-keepalive-timeout={{ .Values.keycloak.timeout }}s
- --server-idle-timeout={{ .Values.keycloak.timeout }}s # default 120s
- --server-read-timeout={{ .Values.keycloak.timeout }}s
- --server-write-timeout={{ .Values.keycloak.timeout }}s
- --resources=uri=/export/MIN*|roles=DCU_EXPORT_USER
- --resources=uri=/export/TRO*|roles=DCU_EXPORT_USER
- --resources=uri=/export/DTEN*|roles=DCU_EXPORT_USER
- --resources=uri=/export/WCS*|roles=WCS_EXPORT_USER
- --resources=uri=/export/MPAM*|roles=MPAM_EXPORT_USER
- --resources=uri=/export/MTS*|roles=MPAM_EXPORT_USER
- --resources=uri=/export/COMP*|roles=COMP_EXPORT_USER
- --resources=uri=/export/FOI*|roles=FOI_EXPORT_USER
- --resources=uri=/export/TO*|roles=TO_EXPORT_USER
- --resources=uri=/export/BF*|roles=BF_EXPORT_USER
- --resources=uri=/export/IEDET*|roles=IEDET_EXPORT_USER
- --resources=uri=/export/POGR*|roles=POGR_EXPORT_USER
- --resources=uri=/export/somu/FOI*|roles=FOI_EXPORT_USER
- --resources=uri=/export/somu/MPAM*|roles=MPAM_EXPORT_USER
- --resources=uri=/export/somu/COMP*|roles=COMP_EXPORT_USER
- --resources=uri=/export/somu/TO*|roles=TO_EXPORT_USER
- --resources=uri=/export/somu/BF*|roles=BF_EXPORT_USER
- --resources=uri=/export/somu/POGR*|roles=POGR_EXPORT_USER
- --resources=uri=/export/topics*|roles=DCU_EXPORT_USER,FOI_EXPORT_USER|require-any-role=true
- --resources=uri=/export/teams*|roles=DCU_EXPORT_USER,WCS_EXPORT_USER,MPAM_EXPORT_USER,COMP_EXPORT_USER,FOI_EXPORT_USER,IEDET_EXPORT_USER,BF_EXPORT_USER,POGR_EXPORT_USER|require-any-role=true
- --resources=uri=/export/users*|roles=DCU_EXPORT_USER,WCS_EXPORT_USER,MPAM_EXPORT_USER,COMP_EXPORT_USER,FOI_EXPORT_USER,IEDET_EXPORT_USER,BF_EXPORT_USER,POGR_EXPORT_USER|require-any-role=true
- --resources=uri=/export/custom/*/refresh|methods=POST|white-listed=true
- --secure-cookie=true
- --http-only-cookie=true
- --enable-default-deny=true
- --enable-refresh-tokens=true
- --encryption-key=$(ENCRYPTION_KEY)
  {{- if not .Values.ingress.internal.enabled }}
  {{/* in production there's only one ingress which means
we can hardcode things for security: */}}
- --redirection-url=https://{{ .Values.keycloak.domain }}
- --cookie-domain={{ .Values.keycloak.domain }}
  {{- end }}
{{- end -}}
