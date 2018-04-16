#!/usr/bin/env bash
set -x -e
eval $(minishift docker-env)
oc adm policy add-scc-to-user privileged -n vertx-chaos-demo -z default
pumba --random \
    --interval 10s \
    kill \
    --signal SIGKILL \
    "re2:.*POD_.*(api-service|backend-service).*"

