Testing This Quickstart
-----------------------
Use the `Makefile` and supporting scripts to test this tutorial.

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
