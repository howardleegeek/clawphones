"""
Lightweight API layer for clawphones with input validation and error handling.

This module provides a small in‑memory API surface to demonstrate
validation and robust error handling without external dependencies.
"""
from typing import Dict, Any, Optional


class APIError(Exception):
    """Custom API error carrying an HTTP-like status code."""

    def __init__(self, status_code: int, message: str):
        super().__init__(message)
        self.status_code = int(status_code)
        self.message = message


def _log(task_id: Optional[str], message: str) -> None:
    """Minimal contextual logger."""
    if task_id:
        print(f"[task_id={task_id}] {message}")
    else:
        print(f"[API] {message}")


class InMemoryAPI:
    """
    Very small in-memory API to illustrate the pattern.
    Public methods simulate endpoints:
      - create_user(data)
      - get_user(user_id)
      - update_user(user_id, data)
    """

    def __init__(self) -> None:
        self._store: Dict[int, Dict[str, Any]] = {}
        self._next_id: int = 1

    # ---------------- validation helpers ----------------
    def _validate_user_input(self, data: Dict[str, Any], *, partial: bool = False) -> None:
        """
        Validate user input.
        - required fields for full creation: username, email, age
        - for partial updates, only check present fields
        """
        if not isinstance(data, dict):
            raise APIError(400, "Invalid input: data must be a dict")

        allowed = {"username", "email", "age"}
        for k in data.keys():
            if k not in allowed:
                raise APIError(400, f"Invalid field: {k}")

        # If not partial, enforce required fields
        if not partial:
            missing = [f for f in ("username", "email", "age") if f not in data]
            if missing:
                raise APIError(400, f"Missing required fields: {', '.join(missing)}")

        # Type checks for provided fields
        if "username" in data and not isinstance(data["username"], str):
            raise APIError(400, "Invalid type: 'username' must be a string")
        if "email" in data and not isinstance(data["email"], str):
            raise APIError(400, "Invalid type: 'email' must be a string")
        if "age" in data:
            if not isinstance(data["age"], int):
                raise APIError(400, "Invalid type: 'age' must be an integer")
            if data["age"] < 0:
                raise APIError(400, "Invalid value: 'age' must be non-negative")

    # ---------------- endpoint-like methods ----------------
    def create_user(self, data: Dict[str, Any], *, task_id: Optional[str] = None) -> Dict[str, Any]:
        _log(task_id, "Creating user with input: {}".format(data))
        self._validate_user_input(data, partial=False)
        user_id = self._next_id
        self._next_id += 1
        user = {
            "id": user_id,
            "username": data["username"],
            "email": data["email"],
            "age": data["age"],
        }
        self._store[user_id] = user
        _log(task_id, f"Created user {user_id}")
        return user

    def get_user(self, user_id: int, *, task_id: Optional[str] = None) -> Dict[str, Any]:
        _log(task_id, f"Fetching user {user_id}")
        if not isinstance(user_id, int) or user_id <= 0:
            raise APIError(400, "Invalid user_id: must be a positive integer")
        user = self._store.get(int(user_id))
        if user is None:
            raise APIError(404, "User not found")
        return dict(user)

    def update_user(self, user_id: int, data: Dict[str, Any], *, task_id: Optional[str] = None) -> Dict[str, Any]:
        _log(task_id, f"Updating user {user_id} with {data}")
        if not isinstance(user_id, int) or user_id <= 0:
            raise APIError(400, "Invalid user_id: must be a positive integer")
        if int(user_id) not in self._store:
            raise APIError(404, "User not found")
        self._validate_user_input(data, partial=True)
        user = self._store[int(user_id)]
        user.update({k: v for k, v in data.items() if k in {"username", "email", "age"}})
        _log(task_id, f"Updated user {user_id}")
        return dict(user)


# Public singleton instance to ease tests
api = InMemoryAPI()
