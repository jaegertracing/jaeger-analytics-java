
DOCKER_IMAGE?=quay.io/jaegertracing/jaeger-analytics-java
DOCKER_TAG?=latest
SPARK_DOCKER_IMAGE?=quay.io/jaegertracing/jaeger-analytics-java-spark

JAEGER_DOCKER_PROTOBUF ?= jaegertracing/protobuf:0.1.0
PROTOC := docker run --rm -u ${shell id -u} -v${PWD}:${PWD} -w${PWD} ${JAEGER_DOCKER_PROTOBUF} --proto_path=${PWD}
PROTO_INCLUDES ?= \
	-Iidl/proto/api_v2 \
	-I/usr/include/github.com/gogo/protobuf

.PHONY: test
test:
	./mvnw clean test

.PHONY: jupyter-docker
jupyter-docker:
	docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .

.PHONY: jupyter-run
jupyter-run:
	docker run --rm -it -p 8888:8888 -p 4041:4040 -p 9001:9001 -e JUPYTER_ENABLE_LAB=yes -v ${PWD}:/home/jovyan/work  ${DOCKER_IMAGE}:${DOCKER_TAG}

.PHONY: spark-run
spark-run:
	echo "Do not forget to set KAFKA_BOOTSTRAP_SERVER"
	java -jar spark/target/jaeger-spark-*-SNAPSHOT.jar

.PHONY: spark-docker
spark-docker:
	docker build -t ${SPARK_DOCKER_IMAGE}:${DOCKER_TAG} -f Dockerfile.spark .

.PHONY: hotrod-run
hotrod-run:
	oc create route edge --service=simple-streaming-collector --port c-binary-trft --insecure-policy=Allow 2>&1 | grep -v "already exists" || true
	docker run --rm -it -e "JAEGER_ENDPOINT=http://$(shell oc get route simple-streaming-collector -o jsonpath="{.spec.host}"):80/api/traces" -p 8080:8080 jaegertracing/example-hotrod:latest

.PHONY: prom-run
prom-run:
	echo "Open browser on :9090"
	docker run --rm --net=host -v ${PWD}/manifests/prometheus-config.yml:/etc/prometheus/prometheus.yml prom/prometheus:latest

.PHONY: grafana-run
grafana-run:
	echo "Open browser on :3000"
	docker run --rm -it --net=host -v ${PWD}/grafana/datasource.yml:/etc/grafana/provisioning/datasources/datasource.yml -v ${PWD}/grafana/dashboard-trace.yml:/etc/grafana/provisioning/dashboards/dashboard-trace.yml -v ${PWD}/grafana/dashboard-tracequality.json:/var/lib/grafana/dashboards/tracequality.json  -v ${PWD}/grafana/dashboard-tracemetrics.json:/var/lib/grafana/dashboards/tracemetrics.json grafana/grafana


.PHONY: proto
proto:
	# Generate gogo, gRPC-Gateway, swagger, go-validators, gRPC-storage-plugin output.
	#
	# -I declares import folders, in order of importance
	# This is how proto resolves the protofile imports.
	# It will check for the protofile relative to each of these
	# folders and use the first one it finds.
	#
	# --gogo_out generates GoGo Protobuf output with gRPC plugin enabled.
	# --grpc-gateway_out generates gRPC-Gateway output.
	# --swagger_out generates an OpenAPI 2.0 specification for our gRPC-Gateway endpoints.
	# --govalidators_out generates Go validation files for our messages types, if specified.
	#
	# The lines starting with Mgoogle/... are proto import replacements,
	# which cause the generated file to import the specified packages
	# instead of the go_package's declared by the imported protof files.
	$(PROTOC) \
		$(PROTO_INCLUDES) \
		--java_out=$(PWD)/proto/src/main/java idl/proto/api_v2/model.proto

	$(PROTOC) \
		$(PROTO_INCLUDES) \
		--java_out=$(PWD)/proto/src/main/java \
		--grpc-java_out=$(PWD)/proto/src/main/java \
		idl/proto/api_v2/query.proto

	$(PROTOC) \
		$(PROTO_INCLUDES) \
		--java_out=$(PWD)/proto/src/main/java /usr/include/protoc-gen-swagger/options/openapiv2.proto

	$(PROTOC) \
		$(PROTO_INCLUDES) \
		--java_out=$(PWD)/proto/src/main/java /usr/include/github.com/gogo/protobuf/gogoproto/gogo.proto

