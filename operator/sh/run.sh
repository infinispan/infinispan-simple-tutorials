#!/usr/bin/env bash


set -e

REPO_URL=$1
NUM_INSTANCES=3


# Login as admin to default project
login() {
  which oc
  oc login -u system:admin
  oc project default
}


# Delete any leftovers
clean() {
  oc delete infinispan example-infinispan || true
}


# Run example Infinispan cluster
run() {
  oc apply -f example-infinispan.yaml
}


# Wait for cluster to form
wait() {
  local podName
  until podName=$(oc get pod -l app=infinispan-pod -o jsonpath="{.items[0].metadata.name}"); do sleep 1; echo Retrying...; done
  local expectedClusterSize=${NUM_INSTANCES}
  local connectCmd="oc exec -it ${podName} -- /opt/jboss/infinispan-server/bin/ispn-cli.sh --connect"
  local clusterSizeCmd="/subsystem=datagrid-infinispan/cache-container=clustered/:read-attribute(name=cluster-size)"

  local members=''
  while [[ "$members" != \"${expectedClusterSize}\" ]];
  do
      members=$(${connectCmd} ${clusterSizeCmd} | grep result | tr -d '\r' | awk '{print $3}')
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
