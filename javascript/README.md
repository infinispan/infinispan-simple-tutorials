# Build requirements

* Node.js
* Infinispan Server 10.1.x or higher 

# Build instructions

    npm install

# Run instructions 

* Start Infinispan server calling `./bin/server.sh` (Linux, OS X, Unix) or 
`bin/server.bat` (Windows).

* Create a cache using the REST interface
`curl -XPOST  http://localhost:11222/rest/v2/caches/javascriptTutorial?template=org.infinispan.DIST_ASYNC`

* Execute: `node index.js`
