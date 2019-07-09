#!/usr/bin/env bash


set -e -x

NAMESPACE=$1


login() {
  which oc
  oc project ${NAMESPACE}
}


# Delete any leftovers
clean() {
  oc delete route example-infinispan || true
}


# Expose service for external access
build() {
  oc expose svc example-infinispan
  sleep 5
}


main() {
  login
  clean
  build
}


main
