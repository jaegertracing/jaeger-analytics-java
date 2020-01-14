
DOCKER_IMAGE?=quay.io/jaegertracing/jaeger-analytics-java
DOCKER_TAG?=latest
SPARK_DOCKER_IMAGE?=quay.io/jaegertracing/jaeger-analytics-java-spark

.PHONY: test
test:
	./mvnw clean test

.PHONY: docker
docker:
	docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .

.PHONY: docker-run
docker-run:
	docker run --rm -it -p 8888:8888 -p 4040:4040 -p 9001:9001 -e JUPYTER_ENABLE_LAB=yes -v ${PWD}:/home/jovyan/work  ${DOCKER_IMAGE}:${DOCKER_TAG}

.PHONY: spark-run
spark-run:
	echo "Do not forget to set KAFKA_BOOTSTRAP_SERVER"
	java -jar target/jaeger-tracedsl-1.0-SNAPSHOT.jar

.PHONY: spark-docker
spark-docker:
	docker build -t ${SPARK_DOCKER_IMAGE}:${DOCKER_TAG} -f Dockerfile.spark .

.PHONY: hotrod-run
hotrod-run:
	oc create route edge --service=simple-streaming-collector --port c-binary-trft --insecure-policy=Allow 2>&1 | grep -v "already exists" || true
	docker run --rm -it -e "JAEGER_ENDPOINT=http://$(shell oc get route simple-streaming-collector -o jsonpath="{.spec.host}"):80/api/traces" -p 8080:8080 jaegertracing/example-hotrod:latest

.PHONY: prom-run
prom-run:
	docker run --rm --net=host -v ${PWD}/manifests/prometheus-config.yml:/etc/prometheus/prometheus.yml prom/prometheus:latest
