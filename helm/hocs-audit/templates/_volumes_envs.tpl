{{- define "volumes.envs" }}
- name: keystore
  emptyDir:
    medium: "Memory"
- name: certs
  emptyDir:
    medium: "Memory"
- name: bundle
  configMap:
    name: bundle
- name: secrets
  emptyDir:
    medium: "Memory"
- name: auth-secrets
  secret:
    secretName: ui-casework-creds
- name: frontend-keycloak-secret
  secret:
    secretName: frontend-keycloak-secret
{{- end -}}
