#!/usr/bin/env bash


set -e -x

REPO_URL=$1


# Login as admin to default project
login() {
  which oc
  oc login -u system:admin
  oc project default
}


# Delete any leftovers
clean() {
  oc delete deployment infinispan-operator || true
  oc delete role infinispan-operator || true
  oc delete crd infinispans.infinispan.org || true
}

# Deploy operator and other components
deploy() {
  oc apply -f ${REPO_URL}/deploy/rbac.yaml
  oc apply -f ${REPO_URL}/deploy/operator.yaml
  oc apply -f ${REPO_URL}/deploy/crd.yaml
}


main() {
  login
  clean
  deploy
}


main
