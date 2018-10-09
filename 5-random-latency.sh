#!/usr/bin/env bash
set -x -e
pumba --debug netem --duration 1m --tc-image gaiadocker/iproute2 delay --time 500 "re2:.*backend-service.*"


