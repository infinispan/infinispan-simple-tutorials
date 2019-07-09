Testing This Quickstart
-----------------------
Use the `Makefile` and supporting scripts to test this tutorial.

**Environment Prerequisites**
* A running [Minikube](https://kubernetes.io/docs/setup/learning-environment/minikube/) cluster.
* Recommended Minikube settings are 4 CPUs and 4GB of memory.

Run the following command to test all tutorial steps:
```bash
$ make all
```

Inspect the `Makefile` and supporting scripts files to find out more about testing.

Testing Custom Descriptors
--------------------------
Pass the `REPO` parameter to test custom descriptors in a specific repository/branch of the Infinispan Operator.

For example, run the following command:
```bash
$ make REPO=myrepository/infinispan-operator/test-branch all
```

Testing Custom Images
---------------------
Pass the `IMAGE` parameter with the `deploy` target to test custom Infinispan Operator images.

For example, run the following command:
```bash
$ make IMAGE=path/to/image deploy
```

**Insecure Registries:**
Do the following to test with custom images that reside in insecure registries:

1. Delete the Minikube Virtual Machine. This step prevents an [issue](https://github.com/kubernetes/minikube/issues/604#issuecomment-247813764) where the insecure registry is ignored.
```bash
$ minikube delete
```
2. Include the `--insecure-registry` flag to enable the insecure registry.
```bash
$ minikube start --insecure-registry <host>:<port>
```
