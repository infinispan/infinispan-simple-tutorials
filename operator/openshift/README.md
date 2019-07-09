Testing This Quickstart
-----------------------
Use the `Makefile` and supporting scripts to test this tutorial.

**NOTE:**
It requires a running OpenShift 4.1 instance with administrator access.

Run:
```bash
$ make all
```

Pass `REPO` to test custom descriptors located in the given repo/branch of the Infinispan Operator as follows:

```bash
$ make REPO=myrepository/infinispan-operator/test-branch all
```

Inspect the `Makefile` and supporting scripts files to find out more about testing.
