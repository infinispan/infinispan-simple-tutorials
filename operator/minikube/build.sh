#!/usr/bin/env bash


set -e -x

NAMESPACE=$1


# Delete any leftovers
clean() {
  kubectl delete route example-infinispan -n ${NAMESPACE} || true
}


# Expose service for external access
build() {
  kubectl expose svc example-infinispan \
    --port 8080 \
    --target-port=8080 \
    --name=example-infinispan-http \
    --type=NodePort \
    -n ${NAMESPACE}

  sleep 5
}


main() {
  clean
  build
}


main
