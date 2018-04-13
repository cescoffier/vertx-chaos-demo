#!/usr/bin/env bash
set -e

source "./env.sh"


########
info "Deploy database"
if oc create -f mongo.json; then
    info "Template provisioned"
fi
if oc new-app mongodb-persistent; then
    info "Deployment started"
fi
########

########

