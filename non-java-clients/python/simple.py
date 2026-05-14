"""Infinispan Python client simple tutorial using the REST API."""

import requests
from requests.auth import HTTPDigestAuth

SERVER_URL = "http://127.0.0.1:11222"
AUTH = HTTPDigestAuth("admin", "password")
CACHE_NAME = "my-cache"


def main():
    # Create the cache
    requests.post(
        f"{SERVER_URL}/rest/v2/caches/{CACHE_NAME}",
        headers={"Content-Type": "application/json"},
        data='{"distributed-cache": {"mode": "SYNC"}}',
        auth=AUTH,
    )

    # Store a value
    requests.put(
        f"{SERVER_URL}/rest/v2/caches/{CACHE_NAME}/key",
        data="value",
        headers={"Content-Type": "text/plain"},
        auth=AUTH,
    )

    # Retrieve the value and print it
    response = requests.get(
        f"{SERVER_URL}/rest/v2/caches/{CACHE_NAME}/key",
        auth=AUTH,
    )
    print(f"key = {response.text}")

    # Delete the cache
    requests.delete(
        f"{SERVER_URL}/rest/v2/caches/{CACHE_NAME}",
        auth=AUTH,
    )


if __name__ == "__main__":
    main()
