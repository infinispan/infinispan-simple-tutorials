This project connects to an Infinispan server via its MCP endpoint.

Always use the Infinispan MCP server tools to interact with Infinispan (manage caches, counters, schemas, entries).
Do not use curl or the REST API directly — the MCP server is the intended interface.

The Infinispan MCP server is configured in `.mcp.json` and provides tools for:
- Cache operations (create, list, get, put, remove, query)
- Counter operations (get, increment, decrement)
- Schema management

Server credentials: admin / password
