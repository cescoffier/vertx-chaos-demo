#!/usr/bin/env bash
set -x -e
pumba --random \
    --interval 10s \
    kill \
    --signal SIGKILL \
    "re2:.*POD_.*(api-service|backend-service).*"

