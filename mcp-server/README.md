# reactividad MCP server

Exposes the `reactividad` menu API (`GET`/`POST`/`DELETE /api/v1/menu`) as MCP tools:
`get_menu`, `create_menu`, `delete_menu`.

## Setup

```bash
cd mcp-server
uv sync
```

## Run the app it talks to

From the repo root, in a separate terminal:

```bash
./mvnw spring-boot:run
```

## Verify with MCP Inspector (no API key needed)

Inspector talks raw MCP protocol to the server directly — it's a protocol tester, not an
LLM client, so it needs no `ANTHROPIC_API_KEY` (unlike `../mcp-server-client`, whose chat
loop does).

```bash
cd mcp-server
npx @modelcontextprotocol/inspector uv run reactividad-mcp-server.py
```

Inspector opens a local web UI where you can list the tools and call `create_menu` →
`get_menu` → `delete_menu` directly against the running app.

## Configuration

Defaults match `application.properties`' dev defaults, so no setup is required against a
local run:

| Env var                      | Default                 |
|-------------------------------|--------------------------|
| `REACTIVIDAD_BASE_URL`         | `http://localhost:8080` |
| `REACTIVIDAD_TOKEN_STANDARD`   | `secret123`              |
| `REACTIVIDAD_TOKEN_PRIME`      | `secret456`              |

`get_menu` uses the `STANDARD` token; `create_menu`/`delete_menu` use `PRIME`, matching the
API's own auth rules (`AuthorizationWebFilter`: `STANDARD` may only `GET`).
