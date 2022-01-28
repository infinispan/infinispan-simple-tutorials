# Use H2 driver with Docker or Podman

```shell
docker run -it -p 11222:11222 -e USER="admin" -e PASS="secret" 
-e SERVER_LIBS="com.h2database:h2:2.0.202" 
quay.io/infinispan/server:13.0

podam run -it -p 11222:11222 -e USER="admin" -e PASS="secret" 
-e SERVER_LIBS="com.h2database:h2:2.0.202" 
quay.io/infinispan/server:13.0
```

# Use H2 driver with in standalone mode

- Download the Infinispan Server 
-Use the install command with the Command Line Interface (CLI) to download the required drivers to the server/lib directory, for example:
```shell
./infinispan-server-13.0.6.Final/bin/cli.sh install com.h2database:h2:2.0.202
```
- Create the user
```shell
./infinispan-server-13.0.6.Final/bin/cli.sh user create admin -p password
```
- Run the server 

```shell
./infinispan-server-13.0.6.Final/bin/server.sh
 ```

- Build the project
```shell
mvn clean install
```

- Run 
```shell
mvn exec:exec
```  
