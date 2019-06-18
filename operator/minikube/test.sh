#!/usr/bin/env bash


set -e


NAMESPACE=$1

# Test using http
test() {
  local host=$(minikube ip)
  local port=$(kubectl get service example-infinispan-http -n operator-testing -o jsonpath="{.spec.ports[0].nodePort}")

  curl -v \
    -X POST \
    -u infinispan:infinispan \
    -H 'Content-type: text/plain' \
    -d 'test-value' \
    http://${host}:${port}/rest/default/test-key

  curl -v \
    -u infinispan:infinispan \
    http://${host}:${port}/rest/default/test-key
}


main() {
  test
}


main
