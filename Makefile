
DOCKER_IMAGE?=pavolloffay/jaeger-tracedsl
DOCKER_TAG?=latest

.PHONY: test
test:
	./mvnw clean test

.PHONY: docker
docker:
	docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .

./PHONY: docker-run
docker-run:
	docker run --rm -it -p 8888:8888 -p 4040:4040 -p 9001:9001 -e JUPYTER_ENABLE_LAB=yes -v ${PWD}:/home/jovyan/work  ${DOCKER_IMAGE}:${DOCKER_TAG}
