import json
import os
import sqlite3
from typing import Any, Dict, List, Optional


class Proxy:
    """A minimal proxy that persists sessions and in-flight messages to SQLite.

    This is intentionally simple to support Byzantine node failure tests:
    - sessions: session_id -> JSON data
    - inflight: message_id -> { session_id, payload }
    The database is stored on disk so that restart recovers previous state.
    """

    def __init__(self, db_path: str):
        self.db_path = db_path
        self.conn: Optional[sqlite3.Connection] = None
        self._open()

    def _open(self) -> None:
        is_new = not os.path.exists(self.db_path)
        self.conn = sqlite3.connect(self.db_path, check_same_thread=False)
        self._ensure_schema()
        if is_new:
            self.conn.execute("PRAGMA journal_mode=WAL")
            self.conn.commit()

    def _ensure_schema(self) -> None:
        cur = self.conn.cursor()
        cur.execute(
            "CREATE TABLE IF NOT EXISTS sessions (session_id TEXT PRIMARY KEY, data TEXT)"
        )
        cur.execute(
            "CREATE TABLE IF NOT EXISTS inflight (message_id TEXT PRIMARY KEY, session_id TEXT, payload TEXT, FOREIGN KEY(session_id) REFERENCES sessions(session_id))"
        )
        self.conn.commit()

    def crash(self) -> None:
        if self.conn:
            self.conn.close()
            self.conn = None

    def restart(self) -> None:
        if self.conn:
            self.conn.close()
        self._open()

    # Session APIs
    def create_session(self, session_id: str, data: Any) -> None:
        cur = self.conn.cursor()
        cur.execute(
            "REPLACE INTO sessions (session_id, data) VALUES (?, ?)",
            (session_id, json.dumps(data)),
        )
        self.conn.commit()

    def get_session(self, session_id: str) -> Optional[Dict[str, Any]]:
        cur = self.conn.cursor()
        cur.execute("SELECT data FROM sessions WHERE session_id = ?", (session_id,))
        row = cur.fetchone()
        if row:
            return json.loads(row[0])
        return None

    # In-flight API
    def add_inflight(self, message_id: str, session_id: str, payload: Any) -> None:
        cur = self.conn.cursor()
        cur.execute(
            "REPLACE INTO inflight (message_id, session_id, payload) VALUES (?, ?, ?)",
            (message_id, session_id, json.dumps(payload)),
        )
        self.conn.commit()

    def get_inflight(self, message_id: str) -> Optional[Dict[str, Any]]:
        cur = self.conn.cursor()
        cur.execute("SELECT session_id, payload FROM inflight WHERE message_id = ?", (message_id,))
        row = cur.fetchone()
        if row:
            sid, payload = row
            return {"session_id": sid, "payload": json.loads(payload)}
        return None

    def list_inflight(self) -> List[Dict[str, Any]]:
        cur = self.conn.cursor()
        cur.execute("SELECT message_id, session_id, payload FROM inflight")
        rows = cur.fetchall()
        return [
            {
                "message_id": mid,
                "session_id": sid,
                "payload": json.loads(payload),
            }
            for (mid, sid, payload) in rows
        ]

    def complete_inflight(self, message_id: str) -> None:
        cur = self.conn.cursor()
        cur.execute("DELETE FROM inflight WHERE message_id = ?", (message_id,))
        self.conn.commit()
