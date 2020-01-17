[![Build Status][ci-img]][ci]

# Jaeger analytics Java

Experimental repository with data analytics models, pipelines for Jaeger tracing data.

Repository contains:
* Graph trace DSL based on Apache Gremlin. It helps to write graph "queries" against a trace
* Spark streaming integration with Kafka for Jaeger topics
* Loading trace from Jaeger query service
* Jupyter notebooks to run examples with data analytic models
* Data analytics models, metrics based on tracing data

### Metrics

The library calculates various metrics from traces. The metrics are
currently exposed in Prometheus format.

Currently these metrics are calculated:

* Trace depth - trace tree height
* Service depth - trace tree height with aggregated spans in a services or in other words maximum service depth of the trace
* Network latency - latency between client and server spans split by service names

```
network_latency_seconds_bucket{client="frontend",server="driver",le="0.005",} 32.0
network_latency_seconds_bucket{client="frontend",server="driver",le="0.01",} 32.0
network_latency_seconds_bucket{client="frontend",server="driver",le="0.025",} 32.0
service_depth_total{quantile="0.7",} 2.0
```

#### Trace quality metrics

Trace quality metrics measure the quality of tracing data reported by services.
These metrics can indicate that further instrumentation is needed or the instrumentation
quality is not high enough.

These metrics are ported from [jaeger-analytics-flink/tracequality](https://github.com/jaegertracing/jaeger-analytics-flink/tree/master/tracequality-job/src/main/java/io/jaegertracing/tracequality/score).
The original design stores results in separate storage table (Cassandra). The intention here is to
export results as metrics and link relevant traces as exemplars (once OSS metrics APIs support that).

* Minimum Jaeger client version - minimum Jaeger client version
* Has client and server tags - span contains client or server tags.

```
trace_quality_server_tag_total{pass="false",service="mysql",} 32.0
trace_quality_server_tag_total{pass="true",service="customer",} 26.0
trace_quality_minimum_client_version_total{pass="false",service="route",version="Go-2.21.1",} 320.0
```

Example Prometheus queries:

```
(trace_quality_server_tag_total{pass="true",service="customer",} / trace_quality_server_tag_total{service="customer",}) * 100
trace_quality_server_tag_total{pass="true",service="customer",} / ignoring (pass,fail) sum without(pass, fail) (trace_quality_server_tag_total)
// if values are missing
(trace_quality_server_tag_total{pass="true",service="mysql",}  / trace_quality_server_tag_total{service="mysql",} ) * 100 or vector(0)
```

### Development

Add annotation processor is needed for IDE configuration. It is used to generate trace DSL.
```
org.apache.tinkerpop.gremlin.process.traversal.dsl.GremlinDslProcessor
```

Build and run
```bash
mvn clean compile exec:java
```

### Configuration

Configuration properties for `SparkRunner`.

* `SPARK_MASTER`: Spark master to submit the job to; Defaults to `local[*]
* `SPARK_STREAMING_BATCH_DURATION`: interval defines the size of the batch in milliseconds; Defaults to `5000
* `KAFKA_JAEGER_TOPIC`: Kafka topic with Jaeger spans; Defaults to `jaeger-spans`
* `KAFKA_BOOTSTRAP_SERVER`: Kafka bootstrap servers; Defaults to `localhost:9092`
* `KAFKA_START_FROM_BEGINNING`: Read kafka topic from the beginning; Default to true
* `PROMETHEUS_PORT`: Prometheus exporter port; Defaults to `9111`
* `TRACE_QUALITY_{language}_VERSION`: Minimum Jaeger client version for trace quality metric; Supported languages `java`, `node`, `python`, `go`; Defaults to latest client versions

## Gremlin documentation
* http://kelvinlawrence.net/book/Gremlin-Graph-Guide.html

## Spark Kafka documentation
* https://spark.apache.org/docs/latest/structured-streaming-kafka-integration.html
* https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html

## Deploy Kafka, Elasticsearch and Jaeger on Kubernetes using operators
The following command creates Jaeger CR which triggers deployment of Jaeger, Kafka and Elasticsearch.
This works only on OpenShift 4.x and prior deploying make sure Jaeger, Strimzi(Kafka) and
Elasticsearch(from OpenShift cluster logging) operators are running.
```
oc create -f manifests/jaeger-auto-provisioned.yaml
```

If you are running on vanilla Kubernetes you can deploy `jaeger-external-kafka-es.yaml` CR and configure
connection strings to Kafka and Elasticsearch.

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
oc get kafka simple-streaming -o jsonpath="{.status.listeners[*].addresses}"
```

### Expose Jaeger collector outside of the cluster
```bash
oc create route edge --service=simple-streaming-collector --port c-binary-trft --insecure-policy=Allow
```

### Deploy Hotrod example application
```bash
oc get routes # get jaeger collector route
docker run --rm -it -e "JAEGER_ENDPOINT=http://host:80/api/traces" -p 8080:8080 jaegertracing/example-hotrod:latest
```

## Get exposed metrics
The streaming job exposes metrics on http://localhost:9001.

## Run Jupyter as docker

The docker image should be published on Docker Hub. If you are modifying the source code of the library then
inject it as volume `-v ${PWD}:/home/jovyan/work` or rebuild the image too see the latest changes.

```bash
make docker
make docker-run
```

Open browser on http://localhost:8888/lab and copy token from the command line. Then navigate to `./work/jupyter/` directory and open notebook.

### Run on Mybinder

[![Launch IJava binder][binder-badge-img]](https://mybinder.org/v2/gh/jaegertracing/jaeger-analytics-java/master) [![Launch IJava lab binder][binder-lab-badge-img]](https://mybinder.org/v2/gh/jaegertracing/jaeger-analytics-java/master?urlpath=lab)

[binder-badge-img]: https://img.shields.io/badge/launch-binder-E66581.svg?logo=data:image/png;base64,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
[binder-lab-badge-img]: https://img.shields.io/badge/launch-binder%20lab-579ACA.svg?logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAFkAAABZCAMAAABi1XidAAAB8lBMVEX///9XmsrmZYH1olJXmsr1olJXmsrmZYH1olJXmsr1olJXmsrmZYH1olL1olJXmsr1olJXmsrmZYH1olL1olJXmsrmZYH1olJXmsr1olL1olJXmsrmZYH1olL1olJXmsrmZYH1olL1olL0nFf1olJXmsrmZYH1olJXmsq8dZb1olJXmsrmZYH1olJXmspXmspXmsr1olL1olJXmsrmZYH1olJXmsr1olL1olJXmsrmZYH1olL1olLeaIVXmsrmZYH1olL1olL1olJXmsrmZYH1olLna31Xmsr1olJXmsr1olJXmsrmZYH1olLqoVr1olJXmsr1olJXmsrmZYH1olL1olKkfaPobXvviGabgadXmsqThKuofKHmZ4Dobnr1olJXmsr1olJXmspXmsr1olJXmsrfZ4TuhWn1olL1olJXmsqBi7X1olJXmspZmslbmMhbmsdemsVfl8ZgmsNim8Jpk8F0m7R4m7F5nLB6jbh7jbiDirOEibOGnKaMhq+PnaCVg6qWg6qegKaff6WhnpKofKGtnomxeZy3noG6dZi+n3vCcpPDcpPGn3bLb4/Mb47UbIrVa4rYoGjdaIbeaIXhoWHmZYHobXvpcHjqdHXreHLroVrsfG/uhGnuh2bwj2Hxk17yl1vzmljzm1j0nlX1olL3AJXWAAAAbXRSTlMAEBAQHx8gICAuLjAwMDw9PUBAQEpQUFBXV1hgYGBkcHBwcXl8gICAgoiIkJCQlJicnJ2goKCmqK+wsLC4usDAwMjP0NDQ1NbW3Nzg4ODi5+3v8PDw8/T09PX29vb39/f5+fr7+/z8/Pz9/v7+zczCxgAABC5JREFUeAHN1ul3k0UUBvCb1CTVpmpaitAGSLSpSuKCLWpbTKNJFGlcSMAFF63iUmRccNG6gLbuxkXU66JAUef/9LSpmXnyLr3T5AO/rzl5zj137p136BISy44fKJXuGN/d19PUfYeO67Znqtf2KH33Id1psXoFdW30sPZ1sMvs2D060AHqws4FHeJojLZqnw53cmfvg+XR8mC0OEjuxrXEkX5ydeVJLVIlV0e10PXk5k7dYeHu7Cj1j+49uKg7uLU61tGLw1lq27ugQYlclHC4bgv7VQ+TAyj5Zc/UjsPvs1sd5cWryWObtvWT2EPa4rtnWW3JkpjggEpbOsPr7F7EyNewtpBIslA7p43HCsnwooXTEc3UmPmCNn5lrqTJxy6nRmcavGZVt/3Da2pD5NHvsOHJCrdc1G2r3DITpU7yic7w/7Rxnjc0kt5GC4djiv2Sz3Fb2iEZg41/ddsFDoyuYrIkmFehz0HR2thPgQqMyQYb2OtB0WxsZ3BeG3+wpRb1vzl2UYBog8FfGhttFKjtAclnZYrRo9ryG9uG/FZQU4AEg8ZE9LjGMzTmqKXPLnlWVnIlQQTvxJf8ip7VgjZjyVPrjw1te5otM7RmP7xm+sK2Gv9I8Gi++BRbEkR9EBw8zRUcKxwp73xkaLiqQb+kGduJTNHG72zcW9LoJgqQxpP3/Tj//c3yB0tqzaml05/+orHLksVO+95kX7/7qgJvnjlrfr2Ggsyx0eoy9uPzN5SPd86aXggOsEKW2Prz7du3VID3/tzs/sSRs2w7ovVHKtjrX2pd7ZMlTxAYfBAL9jiDwfLkq55Tm7ifhMlTGPyCAs7RFRhn47JnlcB9RM5T97ASuZXIcVNuUDIndpDbdsfrqsOppeXl5Y+XVKdjFCTh+zGaVuj0d9zy05PPK3QzBamxdwtTCrzyg/2Rvf2EstUjordGwa/kx9mSJLr8mLLtCW8HHGJc2R5hS219IiF6PnTusOqcMl57gm0Z8kanKMAQg0qSyuZfn7zItsbGyO9QlnxY0eCuD1XL2ys/MsrQhltE7Ug0uFOzufJFE2PxBo/YAx8XPPdDwWN0MrDRYIZF0mSMKCNHgaIVFoBbNoLJ7tEQDKxGF0kcLQimojCZopv0OkNOyWCCg9XMVAi7ARJzQdM2QUh0gmBozjc3Skg6dSBRqDGYSUOu66Zg+I2fNZs/M3/f/Grl/XnyF1Gw3VKCez0PN5IUfFLqvgUN4C0qNqYs5YhPL+aVZYDE4IpUk57oSFnJm4FyCqqOE0jhY2SMyLFoo56zyo6becOS5UVDdj7Vih0zp+tcMhwRpBeLyqtIjlJKAIZSbI8SGSF3k0pA3mR5tHuwPFoa7N7reoq2bqCsAk1HqCu5uvI1n6JuRXI+S1Mco54YmYTwcn6Aeic+kssXi8XpXC4V3t7/ADuTNKaQJdScAAAAAElFTkSuQmCC
[ci-img]: https://github.com/jaegertracing/jaeger-analytics-java/workflows/Test/badge.svg
[ci]: https://github.com/jaegertracing/jaeger-analytics-java/actions
