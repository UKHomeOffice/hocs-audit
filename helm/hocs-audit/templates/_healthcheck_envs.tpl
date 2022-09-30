{{- define "healthcheck.envs" }}
startupProbe:
  httpGet:
    path: /actuator/health/liveness
    port: http
    httpHeaders:
      - name: X-probe
        value: kubelet
  initialDelaySeconds: 10
  periodSeconds: 2
  failureThreshold: 20
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: http
    httpHeaders:
      - name: X-probe
        value: kubelet
  periodSeconds: 2
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: http
    httpHeaders:
      - name: X-probe
        value: kubelet
  periodSeconds: 2
{{- end -}}
