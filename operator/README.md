Spinning Up Clusters with the Infinispan Operator
=================================================
**Authors:** Galder Zamarreño  
**Technologies:** Infinispan, Operator, Kubernetes, OKD, Red Hat OpenShift  
**Summary:** Create and manage Infinispan clusters with the Infinispan Operator.  

About This Tutorial
-------------------
The Infinispan Operator provides operational intelligence to simplify deploying Infinispan on Kubernetes clusters.

This tutorial shows you how to quickly create a two-node cluster using the Infinispan Operator. You then interact with the Infinispan cluster to store and retrieve data.

Prerequisites
-------------
To run this tutorial, you need administrator access to a running cluster and an `oc` client on your `$PATH`.

The Infinispan Operator supports the following environments:

* OKD or Red Hat OpenShift 3.11 or later
* Kubernetes 1.11 or later
* [`yq`](https://github.com/kislyuk/yq) command line utility

For local clusters, you can use Minikube 1.2.0 or later.

Installing the Infinispan Operator
----------------------------------
Do one of the following to install the Infinispan Operator:

* Install the Infinispan Operator from [OperatorHub.io](https://operatorhub.io/).

* Install the Infinispan Operator manually, as follows:
  1. Apply the role and role bindings.
  ```
  $ oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/1.1.0/deploy/rbac.yaml
  ```
    **TIP:** You can replace `1.1.0` with another tagged version of the Infinispan Operator.

  2. Apply the custom resource definition for the operator.
  ```
  $ oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/1.1.0/deploy/crd.yaml
  ```
  3. Apply the template for the operator.
  ```
  $ oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/1.1.0/deploy/operator.yaml
  ```

* Build from source or use the public image to install the Infinispan Operator. See the [Infinispan Operator README](https://github.com/infinispan/infinispan-operator).

Creating Infinispan Clusters
----------------------------
You create Infinispan clusters with custom resource definitions that specify the number of nodes and configuration to use. See the [Infinispan Operator README](https://github.com/infinispan/infinispan-operator) to find out more.

Running the Infinispan Operator Tutorial
----------------------------------------
1. Create an Infinispan cluster with two pods.
```bash
$ oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/1.1.0/deploy/cr/minimal/cr_minimal.yaml
```

2. Verify that the Infinispan cluster forms.
```
$ oc get pods -l app=infinispan-pod
NAME                                   READY     STATUS
example-infinispan-<id_1>     1/1       Running
example-infinispan-<id_2>     1/1       Running
```
Where `<id_*>` is the generated identifier for the pod.
```
$ oc logs example-infinispan-<id_1>
...
INFO  [org.infinispan.CLUSTER] (MSC service thread 1-2)
ISPN000094: Received new cluster view for channel cluster:
[example-infinispan-<id_1>|2] (3)
[example-infinispan-<id_1>,example-infinispan-<id_2>]
```

Storing and Retrieving Data
---------------------------
Connect to your Infinispan cluster and then store some data, as follows:

1. Export the host for the Kubernetes service to a local variable.
```
$ export INFINISPAN_HOST=$(oc get service example-infinispan -o jsonpath={.spec.clusterIP})
```

  The Operator generates authentication secrets when you create Infinispan clusters. The default user is `developer` and the password is a base64 encoded string.

2. Export the password for `developer` to a local variable.
```
$ export PASS=$(oc get secret example-infinispan-generated-secret -o jsonpath="{.data.identities\.yaml}" | base64 --decode | yq -r .credentials[0].password)
```

3. Create a cache (Infinispan 10.x has no default cache).
```bash
curl -v \
    -X POST \
    -u developer:${PASS} \
    ${INFINISPAN_HOST}:11222/rest/v2/caches/test-cache
...
< HTTP/1.1 200 OK
```

4. Store some data through the HTTP endpoint.
```
$ oc exec -it example-infinispan-0 -- \
    curl -v \
    -X POST \
    -u developer:${PASS} \
    -H 'Content-type: text/plain' \
    -d 'test-value' \
    ${INFINISPAN_HOST}:11222/rest/v2/caches/test-cache/test-key
...
< HTTP/1.1 204 No Content
```

5. Retrieve the data from the Infinispan cluster.
```
$ oc exec -it example-infinispan-0 -- \
    curl -v \
    -u developer:${PASS} \
    ${INFINISPAN_HOST}:11222/rest/v2/caches/test-cache/test-key
...
< HTTP/1.1 200 OK
...
test-value
```

  You've successfully completed this tutorial!

  Use the following commands to free up the resources that you created:

  ```bash
  $ oc delete infinispan example-infinispan
  $ oc delete deployment infinispan-operator
  $ oc delete role infinispan-operator
  $ oc delete rolebinding infinispan-operator
  $ oc delete serviceaccount infinispan-operator
  $ oc delete crd infinispans.infinispan.org
  ```
