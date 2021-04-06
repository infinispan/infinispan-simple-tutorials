# Infinispan Simple Tutorials  

[![Build Status](https://travis-ci.org/infinispan/infinispan-simple-tutorials.svg?branch=master)](https://travis-ci.org/infinispan/infinispan-simple-tutorials)

This is a collection of simple tutorials that explain how to use certain
features of Infinispan in the most straightforward way possible.

## Infinispan Client/Server tutorials

Some tutorials require the Infinispan Server running locally.

To run the Server with a container image, check the "Get started" guide 
in the Infinispan Website.
[Getting started with Infinispan](http://infinispan.org)

You can always [download](download link) the latest server distribution and run
from the downloaded files:

```bash
./bin/cli.sh user create admin -p "pass"
./bin/server.sh
```
*IMPORTANT:* 
**Authentication and authorization are implicit since Infinispan 12.1.** 
Creating a `admin` user, will grant the user with the `admin` role.
Check more about implicit authorization in [Authorization Guide](documentation)

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
