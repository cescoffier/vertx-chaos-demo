#!/usr/bin/env bash
set -x -e
#eval $(minishift docker-env)
#--tc-image gaiadocker/iproute2
#--tc-image cescoffier/tc-docker
pumba --debug netem --tc-image gaiadocker/iproute2 --duration 1m loss -p 20 -c 10 "re2:.*api-service.*"

