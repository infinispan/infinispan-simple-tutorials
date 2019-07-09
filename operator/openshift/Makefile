REPO ?= infinispan/infinispan-operator/0.3.0
NAMESPACE ?= operator-testing

.PHONY: deploy run build test all

.DEFAULT_GOAL := all

## deploy:          Deploy operator on OpenShift cluster (requires cluster admin access).
deploy:
	./deploy.sh $(REPO) $(NAMESPACE)

## run:             Run operator to create a cluster.
run:
	./run.sh $(REPO) $(NAMESPACE)

## build:           Build the tutorial demo code.
build:
	./build.sh $(NAMESPACE)

## test:            Test tutorial steps.
test:
	./test.sh $(NAMESPACE)

## all:             Execute all tutorial steps
all: deploy run build test
