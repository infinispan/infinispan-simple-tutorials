# infinispan-simple-tutorials  [![Build Status](https://travis-ci.org/infinispan/infinispan-simple-tutorials.svg?branch=master)](https://travis-ci.org/infinispan/infinispan-simple-tutorials)
Infinispan Simple Tutorials

This is a collection of simple tutorials which explain how to use certain 
features of Infinispan in the most straightfoward way possible.

In order to build the tutorials you will need

- JDK 8
- Apache Maven 3.x
- Some examples use the Infinispan Server. Download the lastest server version and run
```bin/standalone.sh``` from the installation folder

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
