# Vert.x Chaos Demo

## Deployment

1. Get minishift started with at least 8Gb of memory.
2. `oc` must be logged in and connected to minishift (as admin, enable the admin addon if not done already)

```bash
# Deploy mongo
cd kubernetes
./prepare.sh
cd ..

cd backend-service
./deploy.sh
cd ..

cd api-service
./deploy.sh
cd ..

cd chaos-client
mvn clean package


eval $(minishift docker-env)
oc adm policy add-scc-to-user privileged -n vertx-chaos-demo -z default
export URL="http://$(oc get route | grep api-service | awk '{print $2}')"

java -jar target/chaos-client-1.0-SNAPSHOT.jar "${URL}"
```

## Introduce load - WRK

```bash
wrk -c 100 -d 10 ${URL}
``` 

## Pumba

### Kill random pods

```bash
pumba --random --interval 10s kill --signal SIGKILL "re2:.*POD_.*(api-service|backend-service).*"
```

### Packet Loss

```bash
pumba --debug netem --duration 1m --tc-image gaiadocker/iproute2 loss -p 20 -c 10 "re2:.*backend-service.*" 
```

### Latency

```bash   
docker pull gaiadocker/iproute2
pumba --debug netem --duration 1m --tc-image gaiadocker/iproute2 delay --time 500 "re2:.*backend-service.*" 

pumba --debug netem --duration 1m --tc-image gaiadocker/iproute2 delay \
    --time 100 \
    --jitter 30 \
    --correlation 20 \
    "re2:.*backend-service.*"
```