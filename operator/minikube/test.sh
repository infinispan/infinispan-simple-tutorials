#!/usr/bin/env bash


set -e

NAMESPACE=$1


# Test using http
test() {
  local host=$(minikube ip)
  local port=$(kubectl get service example-infinispan-http \
      -n ${NAMESPACE} \
      -o jsonpath="{.spec.ports[0].nodePort}")

  local pass=$(kubectl get secret example-infinispan-app-generated-secret \
      -n ${NAMESPACE} \
      -o jsonpath="{.data.password}" \
      | base64 --decode)

  curl -v \
    -X POST \
    -u developer:${pass} \
    -H 'Content-type: text/plain' \
    -d 'test-value' \
    http://${host}:${port}/rest/default/test-key

  curl -v \
    -u developer:${pass} \
    http://${host}:${port}/rest/default/test-key
}


main() {
  test
}


main
