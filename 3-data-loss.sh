#!/usr/bin/env bash
set -x -e
eval $(minishift docker-env)
pumba --debug netem --duration 1m --tc-image gaiadocker/iproute2 loss -p 20 -c 10 "re2:.*api-service.*"

