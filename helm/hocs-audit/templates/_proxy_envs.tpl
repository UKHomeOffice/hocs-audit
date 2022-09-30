{{- define "proxy.envs" }}
- name: HTTP2
  value: 'TRUE'
- name: PROXY_SERVICE_HOST_1
  value: '127.0.0.1'
- name: PROXY_SERVICE_PORT_1
  value: '8080'
- name: PROXY_SERVICE_HOST_2
  value: '127.0.0.1'
- name: PROXY_SERVICE_PORT_2
  value: '8081'
- name: LOCATIONS_CSV
  value: '/, /export/'
- name: NAXSI_USE_DEFAULT_RULES
  value: 'FALSE'
- name: ENABLE_UUID_PARAM
  value: 'FALSE'
- name: HTTPS_REDIRECT
  value: 'FALSE'
- name: BASIC_AUTH_1
  value: /etc/nginx/authsecrets/htpasswd
- name: SERVER_CERT
  value: /certs/tls.pem
- name: SERVER_KEY
  value: /certs/tls-key.pem
- name: ADD_NGINX_HTTP_CFG
  value: >
    client_header_buffer_size 8k;
    fastcgi_buffer_size 128k;
    fastcgi_buffers 16 64k;
    large_client_header_buffers 4 128k;
    proxy_buffer_size 128k;
    proxy_buffers 4 64k;
    proxy_busy_buffers_size 128k;
    proxy_connect_timeout {{ .Values.keycloak.timeout }};
    proxy_read_timeout {{ .Values.keycloak.timeout }};
    proxy_send_timeout {{ .Values.keycloak.timeout }};
{{- end -}}
