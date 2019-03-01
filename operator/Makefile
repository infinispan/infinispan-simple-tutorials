REPO_URL ?= https://raw.githubusercontent.com/infinispan/infinispan-operator/0.1.0

.PHONY: dep build image push run clean help

.DEFAULT_GOAL := help

## deploy:          Deploy operator on OpenShift cluster (requires cluster admin access).
deploy:
	sh/deploy.sh $(REPO_URL)

## run:             Run operator to create a cluster.
run:
	sh/run.sh $(REPO_URL)

## build:           Build the tutorial demo code.
build:
	sh/build.sh

## test:            Test tutorial steps.
test:
	sh/test.sh

## all:             Execute all tutorial steps
all: deploy run build test
