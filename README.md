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
java -jar target/chaos-client-1.0-SNAPSHOT.jar "http://$(oc get route | grep api-service | awk '{print $2}')"
```

## Introduce load - WRK

```bash
wrk -c 100 -d 10 http://api-service-vertx-chaos-demo.192.168.64.35.nip.io/
``` 

## Pumba

```bash
eval $(minishift docker-env)
oc adm policy add-scc-to-user privileged -n vertx-chaos-demo -z default
```

### Kill random pods

```bash
pumba --random --interval 30s kill --signal SIGKILL "re2:.*POD_.*(api-service|backend-service).*"
```

### Latency

```bash   
docker pull gaiadocker/iproute2
pumba --debug netem --duration 1m --tc-image gaiadocker/iproute2 delay --time 1000 "re2:.*(api-service|backend-service).*" 
```