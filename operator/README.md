Spinning Up Clusters with the Infinispan Operator
=================================================
**Authors:** Galder Zamarreño, Vittorio Rigamonti  
**Technologies:** Infinispan, Operator, Kubernetes, OKD, Red Hat OpenShift  
**Summary:** Create and manage Infinispan clusters with the Infinispan Operator.  

About This Tutorial
-------------------
The Infinispan Operator provides operational intelligence to simplify deploying Infinispan on Kubernetes clusters.

This tutorial shows you how to quickly create a two-node cluster using the Infinispan Operator. You then interact with the Infinispan cluster to store and retrieve data.

Prerequisites
-------------

* `kubectl/oc` client on your `$PATH`
* OKD or Red Hat OpenShift 3.11 or later
* Kubernetes 1.11 or later
* [`yq`](https://github.com/kislyuk/yq) command line utility

For local clusters, you can use Minikube 1.2.0 or later.  
If using `kubectl` client, replace `oc` command with `kubectl -n infinispan-operator` during the tutorial.

Installing the Infinispan Operator
----------------------------------
Do one of the following to install the Infinispan Operator:

* Install the Infinispan Operator from [OperatorHub.io](https://operatorhub.io/).

* Install the Infinispan Operator manually, as follows:
  1. Create a project/namespace `infinispan-operator`
  Apply to that the name the [operator-install.yaml](https://raw.githubusercontent.com/infinispan/infinispan-operator/master/deploy/operator-install.yaml)
  ```bash
  $ oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/master/deploy/operator-install.yaml
  ```

* Build from source or use the public image to install the Infinispan Operator. See the [Infinispan Operator README](https://github.com/infinispan/infinispan-operator).

Creating Infinispan Clusters
----------------------------
You create Infinispan clusters with custom resource definitions that specify the number of nodes and configuration to use. See the [Infinispan Operator README](https://github.com/infinispan/infinispan-operator/tree/master) to find out more.

Running the Infinispan Operator Tutorial
----------------------------------------
1. Create an Infinispan cluster with two pods.
```bash
$ oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/master/deploy/cr/minimal/cr_minimal.yaml
```

2. Verify that the Infinispan cluster forms.
```bash
$ oc get pods -l app=infinispan-pod
NAME                                   READY     STATUS
example-infinispan-0     1/1       Running
example-infinispan-1     1/1       Running
$ oc logs example-infinispan-0
...
INFO  [org.infinispan.CLUSTER] (MSC service thread 1-2)
ISPN000094: Received new cluster view for channel cluster:
[example-infinispan-0-...|1] (2)
[example-infinispan-0-...,example-infinispan-1-....]
```

Storing and Retrieving Data
---------------------------
Connect to your Infinispan cluster and then store some data, as follows:

1. Export the host for the Kubernetes service to a local variable.
```bash
$ export INFINISPAN_HOST='http://'$(oc get service example-infinispan -o jsonpath={.spec.clusterIP})
```
On Openshift with serviceCA enabled the operator will use TLS by default and https needs to be used:
```bash
$ export INFINISPAN_HOST='https://'$(oc get service example-infinispan -o jsonpath={.spec.clusterIP})
```

  The Operator generates authentication secrets when you create Infinispan clusters. The default user is `developer` and the password is a base64 encoded string.

2. Export the password for `developer` to a local variable.
```bash
$ export PASS=$(oc get secret example-infinispan-generated-secret -o jsonpath="{.data.identities\.yaml}" | base64 --decode | yq -r .credentials[0].password)
```

3. Create a cache (Infinispan 12.x has no default cache).
```bash
$ oc exec -it example-infinispan-0 -- \
curl -v \
    -X POST \
    -u developer:${PASS} \
    -k \
    ${INFINISPAN_HOST}:11222/rest/v2/caches/test-cache
...
< HTTP/1.1 200 OK
```

4. Store some data through the HTTP endpoint.
```bash
$ oc exec -it example-infinispan-0 -- \
    curl -v \
    -X POST \
    -u developer:${PASS} \
    -H 'Content-type: text/plain' \
    -d 'test-value' \
    -k \
    ${INFINISPAN_HOST}:11222/rest/v2/caches/test-cache/test-key
...
< HTTP/1.1 204 No Content
```

5. Retrieve the data from the Infinispan cluster.
```bash
$ oc exec -it example-infinispan-0 -- \
    curl -v \
    -u developer:${PASS} \
    -k \
    ${INFINISPAN_HOST}:11222/rest/v2/caches/test-cache/test-key
...
< HTTP/1.1 200 OK
...
test-value
```

  You've successfully completed this tutorial!

  Use the following commands to free up the resources that you created:

  ```bash
  $ oc delete -f https://raw.githubusercontent.com/infinispan/infinispan-operator/master/deploy/cr/minimal/cr_minimal.yaml
  $ oc delete -f https://raw.githubusercontent.com/infinispan/infinispan-operator/master/deploy/operator-install.yaml
  ```
