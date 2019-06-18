REPO ?= infinispan/infinispan-operator/0.2.1
NAMESPACE ?= operator-testing
IMAGE ?= ""

.PHONY: dep build image push run clean help

.DEFAULT_GOAL := help

## deploy:          Deploy operator on OpenShift cluster (requires cluster admin access).
deploy:
	deploy.sh $(REPO) $(NAMESPACE) $(IMAGE)

## run:             Run operator to create a cluster.
run:
	run.sh $(REPO) $(NAMESPACE)

## build:           Build the tutorial demo code.
build:
	build.sh $(NAMESPACE)

## test:            Test tutorial steps.
test:
	test.sh $(NAMESPACE)

## all:             Execute all tutorial steps
all: deploy run build test
