#!/bin/bash
set -euo pipefail # make bash quit if something weird happens

export KUBE_NAMESPACE=${ENVIRONMENT}
export KUBE_TOKEN=${KUBE_TOKEN}
export VERSION=${VERSION}

# passed to keycloak-gatekeeper and nginx for various proxy timeouts
# the default is 60 seconds but audit has long-running queries
export PROXY_TIMEOUT=${PROXY_TIMEOUT:-300}

export DOMAIN="cs"
if [ "${KUBE_NAMESPACE%-*}" == "wcs" ]; then
    export DOMAIN="wcs"
fi

if [[ ${KUBE_NAMESPACE} == *prod ]]
then
    export MIN_REPLICAS="2"
    export MAX_REPLICAS="3"

    export REFRESH_CRON="30 5 * * *"
    export UPTIME_PERIOD="Mon-Sun 05:00-23:00 Europe/London"
    
    export CLUSTER_NAME="acp-prod"
    export KUBE_SERVER=https://kube-api-prod.prod.acp.homeoffice.gov.uk
else 
    export MIN_REPLICAS="1"
    export MAX_REPLICAS="2"

    export REFRESH_CRON="0 9 * * 1-5"
    export UPTIME_PERIOD="Mon-Fri 08:00-18:00 Europe/London"

    export CLUSTER_NAME="acp-notprod"
    export KUBE_SERVER=https://kube-api-notprod.notprod.acp.homeoffice.gov.uk
fi

if [[ "${ENVIRONMENT}" == "wcs-prod" ]] ; then
    export DNS_PREFIX=www.${DOMAIN}
    export KC_REALM=https://sso.digital.homeoffice.gov.uk/auth/realms/HOCS
elif [[ "${ENVIRONMENT}" == "cs-prod" ]] ; then
    export DNS_PREFIX=www.${DOMAIN}
    export KC_REALM=https://sso.digital.homeoffice.gov.uk/auth/realms/hocs-prod
else
    export DNS_PREFIX=${DOMAIN}-notprod
    export KC_REALM=https://sso-dev.notprod.homeoffice.gov.uk/auth/realms/hocs-notprod
fi

export DOMAIN_NAME=${DNS_PREFIX}.homeoffice.gov.uk	

export KUBE_CERTIFICATE_AUTHORITY="https://raw.githubusercontent.com/UKHomeOffice/acp-ca/master/${CLUSTER_NAME}.crt"

echo
echo "Deploying audit ${VERSION} to ${ENVIRONMENT}"
echo "Keycloak realm: ${KC_REALM}"
echo "Redirect URL: ${DOMAIN_NAME}"
echo

cd kd || exit 1

kd --timeout 10m \
    -f deployment.yaml \
    -f service.yaml \
    -f autoscale.yaml \
    -f refreshDcuAggregatedCasesView.yaml
