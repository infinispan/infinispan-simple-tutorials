Spinning Up Clusters with the Infinispan Operator
=================================================
**Authors:** Galder Zamarreño  
**Technologies:** Infinispan, Operator, Kubernetes, OKD  
**Summary:** Create and manage Infinispan clusters on OKD with the Infinispan Operator.  

About This Tutorial
-------------------
The Infinispan Operator provides operational intelligence to simplify deploying Infinispan on Kubernetes clusters.

This tutorial shows you how to quickly create three node cluster using the Infinispan Operator. You then interact with the Infinispan cluster to store and retrieve data.

Installing the Infinispan Operator
----------------------------------
Install the Infinispan Operator from [OperatorHub.io](https://operatorhub.io/).

Alternatively, you can build from source or use the public image to manually install the Infinispan Operator. See the [Infinispan Operator README](https://github.com/infinispan/infinispan-operator).

Creating Infinispan Clusters
----------------------------
You create Infinispan clusters with custom resource definitions that specify the number of nodes and configuration to use. See the [Infinispan Operator README](https://github.com/infinispan/infinispan-operator) to find out more.

Running the Infinispan Operator Tutorial
----------------------------------------
1. Install the Operator components:
```bash
$ oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/0.2.1/deploy/rbac.yaml
$ oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/0.2.1/deploy/operator.yaml
$ oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/0.2.1/deploy/crd.yaml
```

2. Create a 3 node Infinispan cluster:
```bash
$ oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/0.2.1/deploy/cr/cr_minimal.yaml
```

2. Verify that the Infinispan cluster forms.
```
$ oc get pods -l app=infinispan-pod
NAME                                   READY     STATUS
example-infinispan-b58cb5699-727zq     1/1       Running
example-infinispan-b58cb5699-92gr9     1/1       Running
example-infinispan-b58cb5699-dtd97     1/1       Running
```
```
$ oc logs example-infinispan-b58cb5699-727zq
...
INFO  [org.infinispan.CLUSTER] (MSC service thread 1-2)
ISPN000094: Received new cluster view for channel cluster:
[example-infinispan-b58cb5699-dtd97|2] (3)
[example-infinispan-b58cb5699-dtd97,
example-infinispan-b58cb5699-92gr9,
example-infinispan-b58cb5699-727zq]
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
1. Store data through the HTTP endpoint.
```
$ curl -v \
    -X POST \
    -u infinispan:infinispan \
    -H 'Content-type: text/plain' \
    -d 'test-value' \
    ${INFINISPAN_HOST}/rest/default/test-key
...
< HTTP/1.1 200 OK
```

2. Retrieve data.
```
$ curl -v \
    -u infinispan:infinispan \
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

Testing This Quickstart
-----------------------
Use the `Makefile` and supporting scripts to test this tutorial.

Run:
```bash
$ make all
```

Pass `REPO_URL` to test snapshot images of the Infinispan Operator as follows:

```bash
$ make REPO_URL=https://raw.githubusercontent.com/galderz/infinispan-operator/t_release all
```

Inspect the `Makefile` and supporting scripts files to find out more about testing.
