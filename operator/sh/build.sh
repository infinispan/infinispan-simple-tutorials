#!/usr/bin/env bash


set -e -x


# Login as admin to default project
login() {
  which oc
  oc login -u system:admin
  oc project default
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
