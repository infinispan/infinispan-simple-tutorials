Spinning Up Clusters with the Infinispan Operator
=================================================
**Authors:** Galder Zamarreño  
**Technologies:** Infinispan, Operator, Kubernetes, OKD, Red Hat OpenShift  
**Summary:** Create and manage Infinispan clusters with the Infinispan Operator.  

About This Tutorial
-------------------
The Infinispan Operator provides operational intelligence to simplify deploying Infinispan on Kubernetes clusters.

This tutorial shows you how to quickly create a three-node cluster using the Infinispan Operator. You then interact with the Infinispan cluster to store and retrieve data.

Prerequisites
-------------
To run this tutorial, you need administrator access to a running cluster and an `oc` client on your `$PATH`.

The Infinispan Operator supports the following environments:

* OKD or Red Hat OpenShift 3.11 or later
* Kubernetes 1.11 or later

For local clusters, you can use Minikube 1.2.0 or later.

Installing the Infinispan Operator
----------------------------------
Do one of the following to install the Infinispan Operator:

* Install the Infinispan Operator from [OperatorHub.io](https://operatorhub.io/).

* Install the Infinispan Operator manually, as follows:
  1. Apply the role and role bindings.
  ```
  $ oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/0.3.0/deploy/rbac.yaml
  ```
    **TIP:** You can replace `0.3.0` with another tagged version of the Infinispan Operator.

  2. Apply the custom resource definition for the operator.
  ```
  $ oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/0.3.0/deploy/crd.yaml
  ```
  3. Apply the template for the operator.
  ```
  $ oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/0.3.0/deploy/operator.yaml
  ```

* Build from source or use the public image to install the Infinispan Operator. See the [Infinispan Operator README](https://github.com/infinispan/infinispan-operator).

Creating Infinispan Clusters
----------------------------
You create Infinispan clusters with custom resource definitions that specify the number of nodes and configuration to use. See the [Infinispan Operator README](https://github.com/infinispan/infinispan-operator) to find out more.

Running the Infinispan Operator Tutorial
----------------------------------------
1. Create an Infinispan cluster with three nodes.
```bash
$ oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/0.3.0/deploy/cr/cr_minimal.yaml
```

2. Verify that the Infinispan cluster forms.
```
$ oc get pods -l app=infinispan-pod
NAME                                   READY     STATUS
example-infinispan-<id_1>     1/1       Running
example-infinispan-<id_2>     1/1       Running
example-infinispan-<id_3>     1/1       Running
```
Where `<id_*>` is the generated identifier for the pod.
```
$ oc logs example-infinispan-b58cb5699-727zq
...
INFO  [org.infinispan.CLUSTER] (MSC service thread 1-2)
ISPN000094: Received new cluster view for channel cluster:
[example-infinispan-<id_1>|2] (3)
[example-infinispan-<id_1>,example-infinispan-<id_2>,
example-infinispan-<id_3>]
```

Connecting to Infinispan Clusters
---------------------------------
1. Expose the Infinispan service for external access.
```
$ oc expose svc example-infinispan
```

2. Export the host for the public route to a local variable.
```
$ export INFINISPAN_HOST=$(oc get route example-infinispan -o jsonpath="{.spec.host}")
```

Storing and Retrieving Data
---------------------------
Infinispan requires authentication for data access. The default user is `developer` and the Operator automatically generates a password and stores it in a secret when the cluster starts.

1. Export the generated password from the secret to a local variable.
```
$ export PASS=$(oc get secret example-infinispan-app-generated-secret -o jsonpath="{.data.password}" | base64 --decode)
```

2. Store some data through the HTTP endpoint.
```
$ curl -v \
    -X POST \
    -u developer:${PASS} \
    -H 'Content-type: text/plain' \
    -d 'test-value' \
    ${INFINISPAN_HOST}/rest/default/test-key
...
< HTTP/1.1 200 OK
```

2. Retrieve the data from the Infinispan cluster.
```
$ curl -v \
    -u developer:${PASS} \
    ${INFINISPAN_HOST}/rest/default/test-key
...
< HTTP/1.1 200 OK
...
test-value
```

  You've successfully completed this tutorial!

  Use the following commands to free up the resources that you created:

  ```bash
  $ oc delete route example-infinispan
  $ oc delete infinispan example-infinispan
  $ oc delete deployment infinispan-operator
  $ oc delete role infinispan-operator
  $ oc delete rolebinding infinispan-operator
  $ oc delete serviceaccount infinispan-operator
  $ oc delete crd infinispans.infinispan.org
  ```
