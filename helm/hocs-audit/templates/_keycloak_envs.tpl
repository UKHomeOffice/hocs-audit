{{- define "keycloak.envs" }}
- name: ENCRYPTION_KEY
  valueFrom:
    secretKeyRef:
      name: hocs-frontend
      key: encryption_key
{{- end -}}
