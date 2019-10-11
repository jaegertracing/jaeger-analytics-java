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


## Kafka demo with Operators

```bash
oc port-forward simple-streaming-query-85444d76cd-cvs6q 16686
oc port-forward simple-streaming-collector-868894445b-864bs 14268
```

### Expose Kafka outside of cluster and get host:port
Expose Kafka IP address outside of the cluster:
```yaml
external:
    type: nodeport
    tls: false
```

Get Broker IP
```bash
minishift ip
kubectl get service/my-cluster-kafka-0  -n kafka -o go-template='{{range.spec.ports}}{{if .nodePort}}{{.nodePort}}{{"\n"}}{{end}}{{end}}'
```
