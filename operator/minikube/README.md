Testing This Quickstart
-----------------------
Use the `Makefile` and supporting scripts to test this tutorial.

**NOTE:**
It requires a running [Minikube](https://kubernetes.io/docs/setup/learning-environment/minikube/) instance.
Recommended Minikube settings are 4 CPU and 4GB of memory.

Run:
```bash
$ make all
```

Pass `REPO` to test custom descriptors located in the given repo/branch of the Infinispan Operator as follows:

```bash
$ make REPO=myrepository/infinispan-operator/test-branch all
```

If testing a custom operator image, you can use the `deploy` target along with a custom `IMAGE` env variable:

```bash
$ make IMAGE=... deploy
```

Inspect the `Makefile` and supporting scripts files to find out more about testing.

Sometimes custom images might come from insecure registries.
These can be enabled by calling:

```bash
$ minikube start --insecure-registry <host>:<port>
```

**WARNING:**
Insecure registries might be ignore if the minikube machine already existed
(see [issue](https://github.com/kubernetes/minikube/issues/604#issuecomment-247813764)).
So, make sure you call `minikube delete` before starting minikube with an insecure registry.
