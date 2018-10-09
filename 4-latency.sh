#!/usr/bin/env bash
set -x -e
#eval $(minishift docker-env)
pumba --debug netem  --duration 1m --tc-image gaiadocker/iproute2 delay \
    --time 300 \
    --jitter 30 \
    --correlation 20 \
     "re2:.*backend-service.*"


