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
Do one of the following to install the Infinispan Operator:

* Install the Infinispan Operator from [OperatorHub.io](https://operatorhub.io/).

* Install the Infinispan Operator manually, as follows:
  1. Apply the role and role bindings.
  ```
  $ oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/${tag}/deploy/rbac.yaml
  ```
    Where `${tag}` is a tagged version for the Infinispan Operator, for example: `0.3.0`.
  2. Apply the template for the operator.
  ```
  $ oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/${tag}/deploy/operator.yaml
  ```
  3. Apply the custom resource definition for the operator.
  ```
  $ oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/${tag}/deploy/crd.yaml
  ```

* Build from source or use the public image to install the Infinispan Operator. See the [Infinispan Operator README](https://github.com/infinispan/infinispan-operator).

Creating Infinispan Clusters
----------------------------
You create Infinispan clusters with custom resource definitions that specify the number of nodes and configuration to use. See the [Infinispan Operator README](https://github.com/infinispan/infinispan-operator) to find out more.

Running the Infinispan Operator Tutorial
----------------------------------------
1. Install the Operator components:
```bash
$ oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/0.3.0/deploy/rbac.yaml
$ oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/0.3.0/deploy/operator.yaml
$ oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/0.3.0/deploy/crd.yaml
```

2. Create a 3 node Infinispan cluster:
```bash
$ oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/0.3.0/deploy/cr/cr_minimal.yaml
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
1. Access to data in Infinispan is password protected.
The default user is `developer`,
and the password is generated on startup.
Extract and export the password:
```
$ export PASS=$(oc get secret example-infinispan-app-generated-secret -o jsonpath="{.data.password}" | base64 --decode)
```

1. Store data through the HTTP endpoint.
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

2. Retrieve data.
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
