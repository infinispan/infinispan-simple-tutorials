Testing This Quickstart
-----------------------
Use the `Makefile` and supporting scripts to test this tutorial.

**Environment Prerequisites**
* A running Red Hat OpenShift 4.1 cluster with administrator access.

Run the following command to test all tutorial steps:
```bash
$ make all
```

Inspect the `Makefile` and supporting scripts files to find out more about testing.

Testing Custom Descriptors
--------------------------
Pass the `REPO` parameter to test custom descriptors in a specific repository or branch of the Infinispan Operator.

For example, run the following command:
```bash
$ make REPO=myrepository/infinispan-operator/test-branch all
```
