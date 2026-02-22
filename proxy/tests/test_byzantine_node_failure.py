import os
import time
import json
import socket
import subprocess
import pytest


DB_PATH = os.path.join("proxy", "byzantine_test.db")
PORT = 6001


def wait_for_server(port, timeout=5.0):
    end = time.time() + timeout
    while time.time() < end:
        try:
            with socket.create_connection(("127.0.0.1", port), timeout=0.5):
                return True
        except Exception:
            time.sleep(0.1)
    return False


def send_cmd(port, cmd):
    import socket

    with socket.create_connection(("127.0.0.1", port), timeout=2) as s:
        s_file = s.makefile("rwb")
        s_file.write((json.dumps(cmd) + "\n").encode())
        s_file.flush()
        line = s_file.readline()
        return json.loads(line.decode())


def test_byzantine_node_failure_crash_restart():
    # Ensure clean DB path
    if os.path.exists(DB_PATH):
        os.remove(DB_PATH)

    # Start mock backend
    proc = subprocess.Popen(
        ["python3", "proxy/mock_backend.py", "--db", DB_PATH, "--port", str(PORT)],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        bufsize=1,
    )

    try:
        # Wait until server prints listening line
        started = False
        for _ in range(50):
            line = proc.stdout.readline()
            if not line:
                break
            if "listening" in line:
                started = True
                break
        assert started, "Backend did not start in time"
        time.sleep(0.2)

        # 1. Create a session and an in-flight message
        sess_id = "sess-1"
        session_data = '{"user":"alice","roles":["user"]}'
        res = send_cmd(
            PORT, {"cmd": "WRITE_SESSION", "id": sess_id, "data": session_data}
        )
        assert res.get("status") == "OK"

        msg_id = "msg-1"
        res = send_cmd(
            PORT,
            {
                "cmd": "WRITE_MESSAGE",
                "id": msg_id,
                "session_id": sess_id,
                "payload": "payload-123",
                "status": "in_progress",
            },
        )
        assert res.get("status") == "OK"

        # 2. Validate data round-trips
        res = send_cmd(PORT, {"cmd": "GET_SESSION", "id": sess_id})
        assert res.get("status") == "OK" and res.get("data") == session_data

        res = send_cmd(PORT, {"cmd": "GET_MESSAGES", "session_id": sess_id})
        assert res.get("status") == "OK" and len(res.get("messages", [])) == 1

        # 3. Simulate node crash
        proc.terminate()
        proc.wait(timeout=5)

        # 4. Restart backend (recovery of SQLite connection should happen on startup)
        proc = subprocess.Popen(
            ["python3", "proxy/mock_backend.py", "--db", DB_PATH, "--port", str(PORT)],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            bufsize=1,
        )
        started = False
        for _ in range(50):
            line = proc.stdout.readline()
            if not line:
                break
            if "listening" in line:
                started = True
                break
        assert started, "Backend did not restart in time"
        time.sleep(0.2)

        # 5. Verify session and in-flight message persisted after restart
        res = send_cmd(PORT, {"cmd": "GET_SESSION", "id": sess_id})
        assert res.get("status") == "OK" and res.get("data") == session_data

        res = send_cmd(PORT, {"cmd": "GET_MESSAGES", "session_id": sess_id})
        assert res.get("status") == "OK"
        msgs = res.get("messages", [])
        assert (
            len(msgs) == 1
            and msgs[0]["id"] == msg_id
            and msgs[0]["payload"] == "payload-123"
        )

    finally:
        # Attempt graceful shutdown
        try:
            if proc and proc.poll() is None:
                try:
                    send_cmd(PORT, {"cmd": "STOP"})
                except Exception:
                    pass
                proc.terminate()
        except Exception:
            pass
        # Cleanup the test DB file
        try:
            if os.path.exists(DB_PATH):
                os.remove(DB_PATH)
        except Exception:
            pass
