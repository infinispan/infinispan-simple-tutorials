# Use H2 driver with in standalone mode

- Download the Infinispan Server (13 or above version)
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
