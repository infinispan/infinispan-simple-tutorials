# Build requirements

* Node.js
* Infinispan Server 10.1.x or higher 

# Build instructions

    npm install

# Run instructions 

1. Create an admin user `./bin/cli.sh user create admin -p password`
  
2. Start Infinispan server calling `./bin/server.sh` (Linux, OS X, Unix) or 
  `bin/server.bat` (Windows). If you are not using docker for mac check the
  [Getting started with Infinispan](https://infinispan.org/get-started/) 5 minutes tutorial
  to run the container mode.
3. Create a cache named `my-cache` using the [Infinispan Console](http://localhost:11222/)
  ```json
  "distributed-cache": {
    "mode": "SYNC",
    "encoding": {
    "media-type": "text/plain"
    },
    "statistics": true
  }
```
4. Execute: `node index.js`

5. Check with the Infinispan Console [the my-cache cache detail](http://localhost:11222/console/my-cache)
