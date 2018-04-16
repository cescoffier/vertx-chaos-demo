#!/usr/bin/env bash
set -x -e
export URL="http://$(oc get route | grep api-service | awk '{print $2}')"
wrk -c 100 -d 10 ${URL}

