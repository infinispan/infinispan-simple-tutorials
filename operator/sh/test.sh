#!/usr/bin/env bash


set -e


# Login as admin to default project
login() {
  which oc
  oc login -u system:admin
  oc project default
}


# Test using http
test() {
  local routeHost
  routeHost=$(oc get route example-infinispan -o jsonpath="{.spec.host}")

  curl -v \
    -X POST \
    -u infinispan:infinispan \
    -H 'Content-type: text/plain' \
    -d 'test-value' \
    ${routeHost}/rest/default/test-key

  curl -v \
    -u infinispan:infinispan \
    ${routeHost}/rest/default/test-key
}


main() {
  login
  test
}


main
