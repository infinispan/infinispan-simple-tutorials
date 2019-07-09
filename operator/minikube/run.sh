#!/usr/bin/env bash


set -e

REPO=$1
NAMESPACE=$2
NUM_INSTANCES=2

REPO_URL=https://raw.githubusercontent.com/${REPO}


# Delete any leftovers
clean() {
  kubectl delete infinispan example-infinispan -n ${NAMESPACE} || true
}


# Run example Infinispan cluster
run() {
  kubectl apply -f ${REPO_URL}/deploy/cr/cr_minimal.yaml -n ${NAMESPACE}
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
      until podName=$(kubectl get pod -l app=infinispan-pod -o jsonpath="{.items[0].metadata.name}" -n ${NAMESPACE}); do sleep 1; echo Retrying...; done
      connectCmd="kubectl exec -it ${podName} -n ${NAMESPACE} -- /opt/jboss/infinispan-server/bin/ispn-cli.sh --connect"
      members=$(${connectCmd} ${clusterSizeCmd} | grep result | tr -d '\r' | awk '{print $3}')
      if [[ "$members" != \"${expectedClusterSize}\" ]]; then
          # Try alternative location
          connectCmd="kubectl exec -it ${podName} -n ${NAMESPACE} -- /opt/datagrid/bin/cli.sh --connect"
          members=$(${connectCmd} ${clusterSizeCmd} | grep result | tr -d '\r' | awk '{print $3}')
      fi

      echo "Waiting for clusters to form (main: $members)"
      sleep 10
  done
}


main() {
  clean
  run
  wait
}


main
