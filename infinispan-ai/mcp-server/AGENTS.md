This project connects to an Infinispan server via the MCP stdio transport bridge.

Always use the Infinispan MCP server tools to interact with Infinispan (manage caches, counters, schemas, entries).
Do not use curl or the REST API directly. The MCP server is the intended interface.

The Infinispan MCP server is configured in `.mcp.json` using the CLI stdio bridge (via `docker exec`) and provides tools for:
- Cache operations (create, list, get, put, remove, query)
- Counter operations (get, increment, decrement)
- Schema management

Server credentials: admin / password
