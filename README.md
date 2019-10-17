# Jaeger trace DSL

Experimental repository with Jaeger graph DSL.

### Development

Add annotation processor is needed for IDE configuration. It is used to generate trace DSL.
```
org.apache.tinkerpop.gremlin.process.traversal.dsl.GremlinDslProcessor
```

Build and run
```bash
mvn clean compile exec:java
```

## Gremlin documentation
* http://kelvinlawrence.net/book/Gremlin-Graph-Guide.html

## Spark Kafka documentation
* https://spark.apache.org/docs/latest/structured-streaming-kafka-integration.html
* https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html


## Deploy Kafka, Elasticsearch and Jaeger on Kubernetes using operators
```
make kafka
make es
make run
oc create -f manifests/jaeger-simple-streaming.yaml
```

### Expose Kafka outside of cluster and get host:port
Expose Kafka IP address outside of the cluster:
```yaml
listeners:
  # ...
  external:
    type: loadbalancer
    tls: false
```

Get external broker address:
```bash
oc get kafka my-cluster -n kafka -o yaml
```

### Expose Jaeger collector outside of cluster:
```bash
oc create route edge --service=simple-streaming-collector --port c-binary-trft --insecure-policy=Allow
```

### Deploy Hotrod
```bash
oc get routes
docker run --rm -it -e "JAEGER_ENDPOINT=http://host:80/api/traces" -p 8080:8080 jaegertracing/example-hotrod:latest
```

## Get exposed metrics
The streaming job exposes metrics on http://localhost:9001.
