#!/usr/bin/env bash


set -e -x

REPO=$1
NAMESPACE=$2

REPO_URL=https://raw.githubusercontent.com/${REPO}


login() {
  which oc
  oc new-project ${NAMESPACE} || true
  oc project ${NAMESPACE}
}


# Delete any leftovers
clean() {
  oc delete deployment infinispan-operator || true
  oc delete role infinispan-operator || true
  oc delete rolebinding infinispan-operator || true
  oc delete serviceaccount infinispan-operator || true
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
