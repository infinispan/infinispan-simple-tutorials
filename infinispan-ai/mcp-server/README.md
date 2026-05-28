# Infinispan MCP Server Tutorial

This tutorial shows how to enable and use Infinispan's MCP (Model Context Protocol) endpoint
using the CLI stdio transport bridge.

The Infinispan CLI acts as a bridge between MCP clients and the server: it reads JSON-RPC messages
from standard input, forwards them to the server's MCP endpoint, and writes responses to standard output.
This allows AI assistants and LLM-based tools to interact with your Infinispan cluster:
* managing caches
* counters
* schemas

... and more through natural language.

## Prerequisites

- Podman and Podman Compose or Docker and Docker Compose
- An MCP client (e.g., Claude Code, Claude Desktop, an IDE with MCP support)

## Running the Infinispan Server

Run all commands from inside this folder.

Start Infinispan Server with the MCP endpoint enabled:

```bash
docker compose up -d
```

The server is configured with Basic authentication using the credentials `admin` / `password`.

## Connecting an MCP Client

The CLI stdio transport bridge runs inside the Docker container via `docker exec`.
MCP clients launch this command and communicate with it through standard input/output.

### Using the `.mcp.json` file

This folder contains an `.mcp.json` file pre-configured to connect to the local Infinispan MCP
endpoint using the CLI stdio bridge. You can copy it to any project where you want MCP access:

```bash
cp .mcp.json /path/to/your/project/.mcp.json
```

### Claude Code

You can also add the MCP server manually with the Claude Code CLI:

```bash
claude mcp add infinispan -- \
  docker exec -i infinispan-mcp /opt/infinispan/bin/cli.sh mcp http://admin:password@localhost:11222
```

### Claude Desktop

Add the following to your Claude Desktop MCP configuration (`claude_desktop_config.json`):

```json
{
  "mcpServers": {
    "infinispan": {
      "command": "docker",
      "args": ["exec", "-i", "infinispan-mcp", "/opt/infinispan/bin/cli.sh", "mcp", "http://admin:password@localhost:11222"]
    }
  }
}
```

### Using a local CLI installation

If you have the Infinispan server distribution installed locally, you can point directly to the CLI binary
instead of using `docker exec`:

```json
{
  "mcpServers": {
    "infinispan": {
      "command": "/path/to/infinispan-server/bin/infinispan-cli",
      "args": ["mcp", "http://admin:password@localhost:11222"]
    }
  }
}
```

## Using CLI bookmarks

Instead of hardcoding URLs and credentials in your MCP configuration, you can use CLI bookmarks.
A bookmark stores the connection details (URL, credentials, TLS settings) under a name that you
can reference in the `mcp` command.

Create a bookmark:

```bash
docker exec infinispan-mcp /opt/infinispan/bin/cli.sh bookmark set myserver \
  --url http://localhost:11222 --username admin --password password
```

Or if you have a local CLI installation:

```bash
infinispan-cli bookmark set myserver \
  --url http://localhost:11222 --username admin --password password
```

Then reference the bookmark name in your `.mcp.json`:

```json
{
  "mcpServers": {
    "infinispan": {
      "command": "docker",
      "args": ["exec", "-i", "infinispan-mcp", "/opt/infinispan/bin/cli.sh", "mcp", "myserver"]
    }
  }
}
```

Or with a local CLI installation:

```json
{
  "mcpServers": {
    "infinispan": {
      "command": "/path/to/infinispan-server/bin/infinispan-cli",
      "args": ["mcp", "myserver"]
    }
  }
}
```

This is especially useful when connecting to production or secured servers, since credentials
and TLS settings are stored in the CLI configuration rather than in project files.

## Testing Locally

Once Infinispan is running and your MCP client is connected, you can interact with the server
using natural language. Try asking your AI assistant to:

**Create a cache:**
> Create a cache called "my-cache"

**Put an entry:**
> Put the key "greeting" with value "hello world" in my-cache

**Read an entry:**
> Get the value for key "greeting" from my-cache

**List all caches:**
> List all caches

**Create a counter:**
> Create a strong counter called "visitor-count"

You can also verify the MCP endpoint directly with `curl`:

```bash
curl http://localhost:11222/rest/v3/mcp \
  -u admin:password \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}},"id":1}'
```

## Available MCP Capabilities

Once connected, the Infinispan MCP server exposes:

### Tools
- Cache operations (create, list, get, put, remove, query)
- Counter operations (get, increment, decrement)
- Schema management

### Resources
- Server information and configuration
- Audit and access logs

### Prompts
- Documentation search guidance

## Stopping Infinispan

```bash
docker compose down
```
