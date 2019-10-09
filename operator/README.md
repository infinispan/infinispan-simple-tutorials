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

For local clusters, you can use Minikube 1.2.0 or later.

Installing the Infinispan Operator
----------------------------------
Do one of the following to install the Infinispan Operator:

* Install the Infinispan Operator from [OperatorHub.io](https://operatorhub.io/).

* Install the Infinispan Operator manually, as follows:
  1. Apply the role and role bindings.
  ```bash
  oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/1.0.0.Alpha4/deploy/rbac.yaml
  ```
    **TIP:** You can replace `1.0.0.Alpha4` with another tagged version of the Infinispan Operator.

  2. Apply the custom resource definition for the operator.
  ```bash
  oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/1.0.0.Alpha4/deploy/crd.yaml
  ```
  3. Apply the template for the operator.
  ```bash
  oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/1.0.0.Alpha4/deploy/operator.yaml
  ```

* Build from source or use the public image to install the Infinispan Operator. See the [Infinispan Operator README](https://github.com/infinispan/infinispan-operator).

Creating Infinispan Clusters
----------------------------
You create Infinispan clusters with custom resource definitions that specify the number of nodes and configuration to use. See the [Infinispan Operator README](https://github.com/infinispan/infinispan-operator) to find out more.

Running the Infinispan Operator Tutorial
----------------------------------------
1. Create an Infinispan cluster with two pods.
```bash
oc apply -f https://raw.githubusercontent.com/infinispan/infinispan-operator/1.0.0.Alpha4/deploy/cr/minimal/cr_minimal.yaml
```

2. Verify that the Infinispan Operator creates the pods.
```bash
oc get pods -l app=infinispan-pod
NAME                                   READY     STATUS
example-infinispan-0     1/1       Running
example-infinispan-1     1/1       Running
```
3. Check logs to verify pods form clusters.
```bash
oc logs example-infinispan-0
...
INFO  [org.infinispan.CLUSTER] (MSC service thread 1-2)
ISPN000094: Received new cluster view for channel cluster:
[example-infinispan-<id_1>|2] (3)
[example-infinispan-<id_1>,example-infinispan-<id_2>,
example-infinispan-<id_3>]
```

Storing and Retrieving Data
---------------------------
You can interact with the cluster in several way, here two interface are presented: REST and Hotrod.

### Set Application User Credentials
Complete the following steps to set an environment variable with the default application user credentials.

1. Export the host for the public route to a local variable.
```bash
export INFINISPAN_HOST=$(oc get svc example-infinispan-external -o jsonpath="{.spec.externalIPs[0]}")
```

  The Operator generates authentication secrets when you create Infinispan clusters. The default user is `developer` and the password is a base64 encoded string.

2. Get the password for the `developer` user from the default authentication secret.
```bash
oc get secret example-infinispan-generated-secret -o jsonpath="{.data.identities\.yaml}" | base64 --decode
```

Output from the preceding command is as follows:
```
credentials:
  - username: developer
     password: n4Jx@Inm9IaNpQ8a
  - username: operator
     password: vsujrixeZ6kXsOj5
```

3. Export the password for the `developer` user to a local variable.
```bash
export PASS=n4Jx@Inm9IaNpQ8a
```

### Connecting via REST
Connect to your Infinispan cluster and store some data, as follows:

1. Create a cache (Infinispan 10.x has no default cache).
```bash
curl -v \
    -X POST \
    -u developer:${PASS} \
    ${INFINISPAN_HOST}:11222/rest/v2/caches/test-cache
...
< HTTP/1.1 200 OK
```

2. Store some data through the HTTP endpoint.
```bash
curl -v \
    -X POST \
    -u developer:${PASS} \
    -H 'Content-type: text/plain' \
    -d 'test-value' \
    ${INFINISPAN_HOST}:11222/rest/v2/caches/test-cache/test-key
...
< HTTP/1.1 200 OK
```

3. Retrieve the data from the Infinispan cluster.
```bash
curl -v \
    -u developer:${PASS} \
    ${INFINISPAN_HOST}:11222/rest/v2/caches/test-cache/test-key
...
< HTTP/1.1 200 OK
...
test-value
```

### Connecting via Hot Rod
Hot Rod is a custom binary TCP protocol designed for Infinispan client/server interaction. Hot Rod clients are available in several programming languages but this tutorial provides a Java example for demonstration purposes.

1. Change to the `java-client` directory.
```bash
cd java-client
```

2. Run the Hot Rod Java client as follows:
```bash
mvn exec:java -Dexec.mainClass="org.infinispan.App" -Dexec.args="-h 172.30.125.152 -U developer -P ${PASS}"
```
Client code is in `java-client/src/main/java/org/infinispan/App.java`

## Conclusion
You've successfully completed this tutorial!

  Use the following commands to free up the resources that you created:

  ```bash
  oc delete -f https://raw.githubusercontent.com/infinispan/infinispan-operator/1.0.0.Alpha4/deploy/cr/minimal/cr_minimal.yaml
  oc delete -f https://raw.githubusercontent.com/infinispan/infinispan-operator/1.0.0.Alpha4/deploy/operator.yaml
```
