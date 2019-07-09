#!/usr/bin/env bash


set -e

NAMESPACE=$1


login() {
  which oc
  oc project ${NAMESPACE}
}


# Test using http
test() {
  local routeHost
  routeHost=$(oc get route example-infinispan -o jsonpath="{.spec.host}")

  local pass=$(oc get secret example-infinispan-app-generated-secret \
      -o jsonpath="{.data.password}" \
      | base64 --decode)

  curl -v \
    -X POST \
    -u developer:${pass} \
    -H 'Content-type: text/plain' \
    -d 'test-value' \
    ${routeHost}/rest/default/test-key

  curl -v \
    -u developer:${pass} \
    ${routeHost}/rest/default/test-key
}


main() {
  login
  test
}


main
