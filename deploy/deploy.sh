#!/bin/bash
DEPLOY_ACCESS_KEY=$1
DEPLOY_API_URL=$2
DEPLOY_VERSION=$3
curl -X PUT \
    -H "content-type: application/json" \
    -H "Cookie: KuboardUsername=wertycn; KuboardAccessKey=${DEPLOY_ACCESS_KEY}" \
    -d '{"kind":"deployments","namespace":"debug-app","name":"hdmin","images":{"hkccr.ccs.tencentyun.com/debug.icu/hdmin":"hkccr.ccs.tencentyun.com/debug.icu/hdmin:'${DEPLOY_VERSION}'"}}' \
    "${DEPLOY_API_URL}"