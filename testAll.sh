#!/usr/bin/env bash

#npm and docker are mandatory

function runTestAndExitOnError {
   cd $1

   if [ $2 ]; then
      CMD="mvn package exec:exec@$2"
   else
      CMD="mvn package exec:exec"
   fi
   $CMD
   rc=$?
   if [[ $rc -ne 0 ]] ; then
      echo 'tests failing on' $1 ; exit $rc
   else
      cd ..
   fi
}

if [ $1 ]; then
    echo "skip building"
else
    echo "Building ..."
    mvn clean install
    rc=$?
    if [[ $rc -ne 0 ]] ; then
      echo 'project does not compile'; exit $rc
    fi
fi

runTestAndExitOnError clusterexec
runTestAndExitOnError distexec
runTestAndExitOnError distributed
runTestAndExitOnError functional
runTestAndExitOnError jcache
runTestAndExitOnError listen
runTestAndExitOnError map
runTestAndExitOnError query
runTestAndExitOnError replicated
runTestAndExitOnError streams
runTestAndExitOnError spring spring-caching
runTestAndExitOnError spring spring-annotations
runTestAndExitOnError tx
runTestAndExitOnError counter
runTestAndExitOnError multimap
runTestAndExitOnError lock

cd hibernate-cache
runTestAndExitOnError local
runTestAndExitOnError spring-local
cd ..

read -p "Start Infinispan Server, press any key when ready..."

# tests below need infinispan server in local 127.0.0.1
runTestAndExitOnError remote
runTestAndExitOnError remote-listen
runTestAndExitOnError scripting
cd server-tasks
mvn clean package wildfly:deploy
cd ..
runTestAndExitOnError server-tasks

# npm should be installed
echo "NPM should be installed"
cd javascript
npm install
node index.js
cd ..

read -p "Stop Infinispan Server, start Wildfly and press any key..."

# tests below need wildfly in local 127.0.0.1
cd hibernate-cache/wildfly-local
mvn clean package wildfly:deploy
for i in {1..15}; do curl http://localhost:8080/wildfly-local/infinispan/hibernate-cache/$i; done
cd ../..

# test below needs docker !
echo "Docker should be installed"
runTestAndExitOnError spark
