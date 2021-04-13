# Infinispan Simple Tutorials  

[![Build Status](https://travis-ci.org/infinispan/infinispan-simple-tutorials.svg?branch=master)](https://travis-ci.org/infinispan/infinispan-simple-tutorials)

This is a collection of simple tutorials that explain how to use certain
features of Infinispan in the most straightforward way possible.

## Infinispan Server tutorials

Several tutorials use remote caches and require a locally running Infinispan Server.

To run the Server as a container image, visit the "Get Started" page 
in the Infinispan Website.
[Get Started with Infinispan](https://infinispan.org/get-started/)

You can always [download](https://infinispan.org/download/) the latest server distribution and run
from the downloaded files:

```bash
./bin/cli.sh user create admin -p "password"
./bin/server.sh
```
*IMPORTANT:* 
**Infinispan Server requires authentication and authorization by default.** 
Creating a user named `admin` gives you administrative access to Infinispan Server.
You can find more details about users and permissions in [Creating and Modifying Users](https://infinispan.org/docs/stable/titles/server/server.html#creating-users_quickstart)

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
