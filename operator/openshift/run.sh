#!/usr/bin/env bash


set -e

REPO=$1
NAMESPACE=$2
NUM_INSTANCES=2

REPO_URL=https://raw.githubusercontent.com/${REPO}


login() {
  which oc
  oc project ${NAMESPACE}
}


# Delete any leftovers
clean() {
  oc delete infinispan example-infinispan || true
}


# Run example Infinispan cluster
run() {
  oc apply -f ${REPO_URL}/deploy/cr/cr_minimal.yaml
}


# Wait for cluster to form
wait() {
  local expectedClusterSize=${NUM_INSTANCES}
  local clusterSizeCmd="/subsystem=datagrid-infinispan/cache-container=clustered/:read-attribute(name=cluster-size)"

  local podName
  local connectCmd=''
  local members=''
  while [[ "$members" != \"${expectedClusterSize}\" ]];
  do
      until podName=$(oc get pod -l app=infinispan-pod -o jsonpath="{.items[0].metadata.name}"); do sleep 1; echo Retrying...; done
      connectCmd="oc exec -it ${podName} -- /opt/jboss/infinispan-server/bin/ispn-cli.sh --connect"
      members=$(${connectCmd} ${clusterSizeCmd} | grep result | tr -d '\r' | awk '{print $3}')
      if [[ "$members" != \"${expectedClusterSize}\" ]]; then
          # Try alternative location
          connectCmd="oc exec -it ${podName} -- /opt/datagrid/bin/cli.sh --connect"
          members=$(${connectCmd} ${clusterSizeCmd} | grep result | tr -d '\r' | awk '{print $3}')
      fi

      echo "Waiting for clusters to form (main: $members)"
      sleep 10
  done
}


main() {
  login
  clean
  run
  wait
}


main
