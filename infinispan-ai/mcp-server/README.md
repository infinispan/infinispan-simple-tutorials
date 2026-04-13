# Infinispan MCP Server Tutorial

This tutorial shows how to enable and use Infinispan's MCP (Model Context Protocol) endpoint.

Infinispan exposes an MCP endpoint that allows AI assistants and LLM-based tools to interact with
your Infinispan cluster:
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

The MCP endpoint will be available at `http://localhost:11222/rest/v3/mcp`.

The server is configured with Basic authentication using the credentials `admin` / `password`.

## Connecting an MCP Client

### Using the `.mcp.json` file

This folder contains an `.mcp.json` file pre-configured to connect to the local Infinispan MCP
endpoint with Basic authentication. You can copy it to any project where you want MCP access:

```bash
cp .mcp.json /path/to/your/project/.mcp.json
```

### Claude Code

You can also add the MCP server manually with the Claude Code CLI:

```bash
claude mcp add infinispan --transport http http://localhost:11222/rest/v3/mcp \
  --header "Authorization: Basic YWRtaW46cGFzc3dvcmQ="
```

### Claude Desktop

Add the following to your Claude Desktop MCP configuration (`claude_desktop_config.json`):

```json
{
  "mcpServers": {
    "infinispan": {
      "type": "http",
      "url": "http://localhost:11222/rest/v3/mcp",
      "headers": {
        "Authorization": "Basic YWRtaW46cGFzc3dvcmQ="
      }
    }
  }
}
```

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
