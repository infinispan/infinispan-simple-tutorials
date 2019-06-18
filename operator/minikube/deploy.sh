#!/usr/bin/env bash


set -e -x

REPO=$1
NAMESPACE=$2
IMAGE=$3

REPO_URL=https://raw.githubusercontent.com/${REPO}


# Delete any leftovers
clean() {
  kubectl delete namespace ${NAMESPACE} || true
}


# Deploy operator and other components
deploy() {
  kubectl create namespace ${NAMESPACE}

  kubectl apply -f ${REPO_URL}/deploy/rbac.yaml -n ${NAMESPACE}

  if [ -z "${IMAGE}" ]; then
    kubectl apply -f ${REPO_URL}/deploy/operator.yaml -n ${NAMESPACE}
  else
    local targetDir='../target'
    mkdir ${targetDir} || true
    curl -o ${targetDir}/operator.yaml ${REPO_URL}/deploy/operator.yaml
    sed -ri 's@^(\s*)(image\s*:\s*jboss.*\s*$)@\1image: '"${IMAGE}"'@' ${targetDir}/operator.yaml
    kubectl apply -f ${targetDir}/operator.yaml -n ${NAMESPACE}
  fi

  kubectl apply -f ${REPO_URL}/deploy/crd.yaml -n ${NAMESPACE}
}


main() {
  clean
  deploy
}


main
