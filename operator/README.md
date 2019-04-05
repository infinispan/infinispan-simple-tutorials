Infinispan Operator for OpenShift
=================================
**Authors:** Galder Zamarreño  
**Technologies:** Infinispan, Red Hat OpenShift, Operator  
**Summary:** Demonstrate using Infinispan Operator to create and manage Infinispan clusters on Red Hat OpenShift.   

About This Tutorial
-------------------
This tutorial provides a guide to creating Infinispan clusters using the Infinispan Operator on Red Hat OpenShift.

After the initial set up, the tutorial demonstrates that you can interact with the Infinispan cluster to store and retrieve data.

Before You Begin
----------------
You should have a running installation of an OpenShift version that supports running Operators.
At the time of writing, OpenShift 4 or 3.11 support running them.

To go through this tutorial, you must have admin access to the OpenShift cluster.
This is because installing Operators require admin privileges.

**NOTE:** To simplify things, the tutorial assumes you run through it all using the admin account.
However, it should be possible to install the Operator as admin, and do the rest of tutorial as a non-admin user. 

Runing the Infinispan Operator Tutorial
---------------------------------------
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

3. Verify that the 3 node cluster forms:
```bash
$ oc get pods -l app=infinispan-pod
NAME                                   READY     STATUS      RESTARTS   AGE
example-infinispan-b58cb5699-727zq     1/1       Running     0          46m
example-infinispan-b58cb5699-92gr9     1/1       Running     0          46m
example-infinispan-b58cb5699-dtd97     1/1       Running     0          46m

$ oc logs example-infinispan-b58cb5699-727zq
...
14:27:34,041 INFO  [org.infinispan.CLUSTER] (MSC service thread 1-2) ISPN000094: Received new cluster view for channel cluster: [example-infinispan-b58cb5699-dtd97|2] (3) [example-infinispan-b58cb5699-dtd97, example-infinispan-b58cb5699-92gr9, example-infinispan-b58cb5699-727zq]
```

4. Expose the Infinispan service so that data can be stored and retrieved from outside OpenShift:
```bash
oc expose svc example-infinispan
```

5. Find out the host for the public route exposed:
```bash
$ export INFINISPAN_HOST=$(oc get route example-infinispan -o jsonpath="{.spec.host}")
```
  **NOTE:** Route host format varies depending on environment and OpenShift version used.
  For this tutorial, the host name is assigned to a local variable that can be used in next steps. 

6. Store test data using the HTTP endpoint:
```bash
$ curl -v \
    -X POST \
    -u infinispan:infinispan \
    -H 'Content-type: text/plain' \
    -d 'test-value' \
    ${INFINISPAN_HOST}/rest/default/test-key
...
< HTTP/1.1 200 OK
```

7. Verify that the test data can be retrieved:
```bash
$ curl -v \
    -u infinispan:infinispan \
    ${INFINISPAN_HOST}/rest/default/test-key
...
< HTTP/1.1 200 OK
...
test-value
```

  You've successfully completed this tutorial!

  Resources created by this tutorial can be freed up calling:

  ```bash
  $ oc delete route example-infinispan
  $ oc delete infinispan example-infinispan
  $ oc delete deployment infinispan-operator
  $ oc delete role infinispan-operator
  $ oc delete rolebinding infinispan-operator
  $ oc delete serviceaccount infinispan-operator
  $ oc delete crd infinispans.infinispan.org
  ```

Testing
-------
A `Makefile` and supporting scripts are provided to test the tutorial.
Testing can be done executing:

```bash
$ make all
```

Snapshot Infinispan Operator images can be tested by passing alternative repository URLs:

```bash
$ make REPO_URL=https://raw.githubusercontent.com/galderz/infinispan-operator/t_release all
```

Inspect the `Makefile` and supporting scripts files to find out more about how testing works. 
