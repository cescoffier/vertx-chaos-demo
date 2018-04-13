#!/usr/bin/env bash
# Colors are important
export RED='\033[0;31m'
export NC='\033[0m' # No Color
export YELLOW='\033[0;33m'
export BLUE='\033[0;34m'

export OS_PROJECT_NAME="vertx-chaos-demo"

function warning {
    echo -e "  ${YELLOW} $1 ${NC}"
}

function error {
    echo -e "  ${RED} $1 ${NC}"

}

function info {
    echo -e "  ${BLUE} $1 ${NC}"
}

oc version

if oc new-project "${OS_PROJECT_NAME}"; then
    info "Project ${OS_PROJECT_NAME} created"
else
    info "Reusing existing project ${OS_PROJECT_NAME}"
    oc project "${OS_PROJECT_NAME}"
fi