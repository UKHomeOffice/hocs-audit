{{- define "truststore.envs" }}
- --certs=/certs
- --command=/usr/bin/create-keystore.sh /certs/tls.pem /certs/tls-key.pem /etc/ssl/certs/acp-root.crt
- --domain={{ include "hocs-app.name" . }}.{{ .Release.Namespace }}.svc.cluster.local
- --onetime=true
{{- end -}}
