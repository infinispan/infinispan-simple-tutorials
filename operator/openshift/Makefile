REPO ?= infinispan/infinispan-operator/1.1.0
REPO_URL = https://raw.githubusercontent.com/${REPO}
NAMESPACE ?= operator-testing

.PHONY: create-project clean-operator deploy clean-infinispan run put get test all

.DEFAULT_GOAL := all

## create-project  Create OpenShift project
create-project:
	oc new-project ${NAMESPACE} || true

## clean-operator  Clean all created resources
clean-operator:
	oc delete infinispan example-infinispan || true
	oc delete deployment infinispan-operator || true
	oc delete role infinispan-operator || true
	oc delete rolebinding infinispan-operator || true
	oc delete serviceaccount infinispan-operator || true
	oc delete crd infinispans.infinispan.org || true

## deploy:          Deploy the Operator on an OpenShift cluster (requires cluster admin access).
deploy: create-project clean-operator
	oc apply -f ${REPO_URL}/deploy/crd.yaml
	oc apply -f ${REPO_URL}/deploy/rbac.yaml
	oc apply -f ${REPO_URL}/deploy/operator.yaml

## clean-infinispan    Clean Infinispan example
clean-infinispan:
	oc delete infinispan example-infinispan || true

## run:             Run the Operator to create a cluster.
run: clean-infinispan
	oc apply -f ${REPO_URL}/deploy/cr/minimal/cr_minimal.yaml
	until oc wait --for condition=wellFormed --timeout=240s infinispan/example-infinispan; \
	do echo "Waiting for Infinispan"; sleep 2; \
	done

host = `oc get service example-infinispan -o jsonpath={.spec.clusterIP}`

password = `oc get secret \
    example-infinispan-generated-secret \
    -o jsonpath="{.data.identities\.yaml}" \
    | base64 -d \
    | yq -r .credentials[0].password`

## put:             Put data into Infinispan
put:
	host=${call host}; \
	password=${call password}; \
	oc exec -it example-infinispan-0 -- \
	curl --fail -v \
	-X POST \
	-u developer:${password} \
	-H 'Content-type: text/plain' \
	-d 'test-value' \
	${host}:11222/rest/v2/caches/default/test-key

## get:             Get data from Infinispan
get:
	host=${call host}; \
	password=${call password}; \
	oc exec -it example-infinispan-0 -- \
	curl --fail -v \
	-u developer:${password} \
	${host}:11222/rest/v2/caches/default/test-key

## test:            Test the tutorial steps.
test: put get

## all:             Execute all tutorial steps
all: deploy run test
