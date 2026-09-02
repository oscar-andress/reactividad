import os
from typing import Any, Optional

import httpx
from mcp.server.fastmcp import FastMCP
from pydantic import Field

BASE_URL = os.environ.get("REACTIVIDAD_BASE_URL", "http://localhost:8080")
TOKEN_STANDARD = os.environ.get("REACTIVIDAD_TOKEN_STANDARD", "secret123")
TOKEN_PRIME = os.environ.get("REACTIVIDAD_TOKEN_PRIME", "secret456")

mcp = FastMCP("ReactividadMenu", log_level="ERROR")


class ReactividadApiError(Exception):
    """Raised when the reactividad API returns a non-2xx response."""


async def _request(
    method: str,
    path: str,
    token: str,
    json: Optional[dict[str, Any]] = None,
) -> httpx.Response:
    async with httpx.AsyncClient(base_url=BASE_URL, timeout=10.0) as client:
        response = await client.request(method, path, headers={"auth-token": token}, json=json)

    if response.status_code >= 400:
        try:
            body = response.json()
            message = body.get("message", response.text)
            error_code = body.get("errorCode", str(response.status_code))
        except ValueError:
            message = response.text or response.reason_phrase
            error_code = str(response.status_code)
        raise ReactividadApiError(f"[{error_code}] {message}")

    return response


@mcp.tool(
    name="get_menu",
    description="Fetch a food menu by its UUID from the reactividad API.",
)
async def get_menu(
    menu_id: str = Field(description="UUID of the menu to fetch"),
) -> str:
    response = await _request("GET", f"/api/v1/menu/{menu_id}", TOKEN_STANDARD)
    return response.text


@mcp.tool(
    name="create_menu",
    description="Create a new food menu in the reactividad API.",
)
async def create_menu(
    menu_title: str = Field(description="Title of the menu, up to 50 characters"),
    menu_description: str = Field(description="Description of the menu, up to 50 characters"),
) -> str:
    response = await _request(
        "POST",
        "/api/v1/menu/",
        TOKEN_PRIME,
        json={"menuTitle": menu_title, "menuDescription": menu_description},
    )
    return response.text


@mcp.tool(
    name="delete_menu",
    description="Delete a food menu by its UUID from the reactividad API.",
)
async def delete_menu(
    menu_id: str = Field(description="UUID of the menu to delete"),
) -> str:
    await _request("DELETE", f"/api/v1/menu/{menu_id}", TOKEN_PRIME)
    return f"Menu {menu_id} deleted."


if __name__ == "__main__":
    mcp.run(transport="stdio")
