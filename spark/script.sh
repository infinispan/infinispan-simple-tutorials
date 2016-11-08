#!/usr/bin/env bash

function wait_for_ispn() {
  until `docker exec -t $1 /opt/jboss/infinispan-server/bin/ispn-cli.sh -c ":read-attribute(name=server-state)"  | grep -q running`; do
    sleep 1
    echo "Waiting for the server to start..."
  done
}

function get_mvn_variable() {
   mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=$1 | grep -v '\[.*\]'
}

CONNECTOR_VERSION=$(get_mvn_variable "version.spark-connector")
SPARK_VERSION=$(get_mvn_variable "version.spark")
INFINISPAN_VERSION=$(get_mvn_variable "version.infinispan")

echo "Launching containers..."
INFINISPAN_ID=$(docker run -v $PWD:/usr/local/code -ti -d jboss/infinispan-server:$INFINISPAN_VERSION)
SPARK_ID=$(docker run -v $PWD:/usr/local/code -ti -d gustavonalle/spark:$SPARK_VERSION)

wait_for_ispn $INFINISPAN_ID
INFINISPAN_ADDRESS=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' $INFINISPAN_ID)

echo "Server started, running job"
docker exec -t $SPARK_ID /usr/local/spark/bin/spark-shell --jars /usr/local/code/target/infinispan-simple-tutorials-spark-1.0.0-SNAPSHOT.jar --class org.infinispan.tutorial.simple.spark.SimpleSparkJob $INFINISPAN_ADDRESS --packages org.infinispan:infinispan-spark_2.11:$CONNECTOR_VERSION --conf spark.io.compression.codec=lz4
docker kill $INFINISPAN_ID $SPARK_ID
