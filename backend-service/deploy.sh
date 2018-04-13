#!/usr/bin/env bash
export NAME=backend-service

echo "Creating build"
oc new-build --binary --name=${NAME} -l app=${NAME}

echo "Building app"
mvn clean package

echo "Triggering build"
oc start-build ${NAME} --from-dir=. --follow

echo "Creating deployment config, service and route"
oc apply -f kubernetes/deployment.yaml
oc apply -f kubernetes/service.yaml
oc apply -f kubernetes/route.yaml
