import os
import json
from pathlib import Path

import pytest

from proxy.proxy import Proxy


def test_byzantine_node_failure_recovery(tmp_path: Path):
    # Use a temporary file-based SQLite DB to simulate a real persistence layer
    db_path = tmp_path / "proxy_byz_test.db"
    db_path_str = str(db_path)

    # Start proxy and create a session with in-flight message
    p = Proxy(db_path_str)
    p.create_session("sess1", {"user": "Alice", "state": "active"})
    p.add_inflight("m1", "sess1", {"type": "update", "seq": 1})

    # Simulate node crash and restart
    p.crash()
    p2 = Proxy(db_path_str)

    # Verify session data survived
    sess = p2.get_session("sess1")
    assert sess == {"user": "Alice", "state": "active"}

    # Verify in-flight message survived
    inflight = p2.list_inflight()
    assert any(item["message_id"] == "m1" and item["session_id"] == "sess1" and item["payload"] == {"type": "update", "seq": 1} for item in inflight)

    # Complete the in-flight message and ensure it is removed
    p2.complete_inflight("m1")
    inflight_after = p2.list_inflight()
    assert all(item["message_id"] != "m1" for item in inflight_after)

    # Ensure that a subsequent restart does not crash and still has the session data
    p3 = Proxy(db_path_str)
    assert p3.get_session("sess1") == {"user": "Alice", "state": "active"}
