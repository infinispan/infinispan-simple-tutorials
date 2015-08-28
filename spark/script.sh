#!/usr/bin/env bash
function wait_for_ispn_on() {
  until `docker exec -t $1 /usr/local/infinispan-server/bin/jboss-cli.sh -c ":read-attribute(name=server-state)"  | grep -q running`; do
    sleep 1
    echo "Waiting for the server to start..."
  done
}

CONTAINER_ID=$(docker run -v $PWD:/usr/local/code -ti -d gustavonalle/infinispan-spark)
wait_for_ispn_on $CONTAINER_ID
echo "Server started, running job"
docker exec -t $CONTAINER_ID /usr/local/spark/bin/spark-shell --jars /usr/local/code/target/infinispan-simple-tutorials-spark-1.0.0-SNAPSHOT.jar --class org.infinispan.tutorial.simple.spark.SimpleSparkJob --packages org.infinispan:infinispan-spark_2.10:0.1
docker kill $CONTAINER_ID
