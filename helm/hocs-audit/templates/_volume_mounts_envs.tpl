{{- define "volumeMounts.envs" }}
- mountPath: /etc/keystore
  name: keystore
  readOnly: true
{{- end -}}
