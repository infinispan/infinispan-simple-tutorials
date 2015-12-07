#!/usr/bin/env bash
function wait_for_ispn_on() {
  until `docker exec -t $1 /opt/jboss/infinispan-server/bin/ispn-cli.sh -c ":read-attribute(name=server-state)"  | grep -q running`; do
    sleep 1
    echo "Waiting for the server to start..."
  done
}
echo "Launching containers..."
INFINISPAN_ID=$(docker run -v $PWD:/usr/local/code -ti -d gustavonalle/infinispan-server)
SPARK_ID=$(docker run -v $PWD:/usr/local/code -ti -d gustavonalle/spark)
wait_for_ispn_on $INFINISPAN_ID
INFINISPAN_ADDRESS=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' $INFINISPAN_ID)
echo "Server started, running job"
docker exec -t $SPARK_ID /usr/local/spark/bin/spark-shell --jars /usr/local/code/target/infinispan-simple-tutorials-spark-1.0.0-SNAPSHOT.jar --class org.infinispan.tutorial.simple.spark.SimpleSparkJob $INFINISPAN_ADDRESS --packages org.infinispan:infinispan-spark_2.10:0.2
docker kill $INFINISPAN_ID $SPARK_ID
