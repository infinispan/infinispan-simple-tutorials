REPO ?= infinispan/infinispan-operator/0.3.0
NAMESPACE ?= operator-testing
IMAGE ?= ""

.PHONY: deploy run build test all

.DEFAULT_GOAL := all

## deploy:          Deploy the Operator on a Minikube cluster (requires cluster admin access).
deploy:
	./deploy.sh $(REPO) $(NAMESPACE) $(IMAGE)

## run:             Run the Operator to create a cluster.
run:
	./run.sh $(REPO) $(NAMESPACE)

## build:           Build the tutorial demo code.
build:
	./build.sh $(NAMESPACE)

## test:            Test the tutorial steps.
test:
	./test.sh $(NAMESPACE)

## all:             Execute all tutorial steps.
all: deploy run build test
