# Infinispan Simple Tutorials

This is a collection of simple tutorials that explain how to use certain
features of Infinispan in the most straightforward way possible.

Check the documentation in the [Infinispan website](https://infinispan.org/infinispan-simple-tutorials)


## Building Tutorials

In order to build the tutorials you will need

- JDK 8
- Apache Maven 3.x
- Some examples use the Infinispan Server. Download the lastest server version and run `bin/server.sh` from the installation directory

You can compile and run each individual tutorial by changing to its folder
and invoking:

```bash
mvn clean package
mvn exec:exec
```

Tutorials that involve deploying an archive to Wildfly are first deployed like this:

```bash
mvn clean package
mvn wildfly:deploy
```

Then, check the tutorial for a particular URL to interact with.

## To go further
Check [Infinispan Demos](https://github.com/infinispan-demos/links) repository
