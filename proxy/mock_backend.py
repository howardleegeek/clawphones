#!/usr/bin/env python3
import argparse
import json
import sqlite3
import socket
import threading
from socketserver import ThreadingMixIn, StreamRequestHandler, TCPServer


class DB:
    def __init__(self, path: str):
        self.path = path
        self.conn = None
        self.lock = threading.Lock()
        self._init_db()

    def _init_db(self):
        self.conn = sqlite3.connect(self.path, check_same_thread=False)
        cur = self.conn.cursor()
        cur.execute(
            "CREATE TABLE IF NOT EXISTS sessions(id TEXT PRIMARY KEY, data TEXT)"
        )
        cur.execute(
            "CREATE TABLE IF NOT EXISTS messages(id TEXT PRIMARY KEY, session_id TEXT, payload TEXT, status TEXT)"
        )
        self.conn.commit()

    def close(self):
        if self.conn:
            self.conn.close()

    # Thread-safe DB operations
    def write_session(self, id_: str, data: str):
        with self.lock:
            self.conn.execute(
                "INSERT OR REPLACE INTO sessions(id, data) VALUES(?, ?)", (id_, data)
            )
            self.conn.commit()

    def write_message(self, id_: str, session_id: str, payload: str, status: str):
        with self.lock:
            self.conn.execute(
                "INSERT OR REPLACE INTO messages(id, session_id, payload, status) VALUES(?, ?, ?, ?)",
                (id_, session_id, payload, status),
            )
            self.conn.commit()

    def get_session(self, id_: str):
        with self.lock:
            cur = self.conn.execute("SELECT data FROM sessions WHERE id=?", (id_,))
            row = cur.fetchone()
            return row[0] if row else None

    def get_messages(self, session_id: str):
        with self.lock:
            cur = self.conn.execute(
                "SELECT id, payload, status FROM messages WHERE session_id=?",
                (session_id,),
            )
            return [
                {"id": r[0], "payload": r[1], "status": r[2]} for r in cur.fetchall()
            ]


class Handler(StreamRequestHandler):
    def handle(self):
        if not hasattr(self.server, "db") or self.server.db is None:
            self.server.db = DB(self.server.db_path)
        db = self.server.db
        while True:
            line = self.rfile.readline()
            if not line:
                break
            try:
                req = json.loads(line.decode())
            except Exception:
                self.wfile.write(
                    (
                        json.dumps({"status": "ERROR", "message": "invalid json"})
                        + "\n"
                    ).encode()
                )
                continue

            cmd = req.get("cmd")
            if cmd == "WRITE_SESSION":
                db.write_session(req["id"], req["data"])
                self.wfile.write((json.dumps({"status": "OK"}) + "\n").encode())
            elif cmd == "WRITE_MESSAGE":
                db.write_message(
                    req["id"], req["session_id"], req["payload"], req["status"]
                )
                self.wfile.write((json.dumps({"status": "OK"}) + "\n").encode())
            elif cmd == "GET_SESSION":
                data = db.get_session(req["id"])
                self.wfile.write(
                    (json.dumps({"status": "OK", "data": data}) + "\n").encode()
                )
            elif cmd == "GET_MESSAGES":
                msgs = db.get_messages(req["session_id"])
                self.wfile.write(
                    (json.dumps({"status": "OK", "messages": msgs}) + "\n").encode()
                )
            elif cmd == "STOP":
                self.wfile.write((json.dumps({"status": "OK"}) + "\n").encode())
                # Graceful stop
                self.server.shutdown()
                break
            else:
                self.wfile.write(
                    (
                        json.dumps({"status": "ERROR", "message": "unknown cmd"}) + "\n"
                    ).encode()
                )


class ThreadedTCPServer(TCPServer, ThreadingMixIn):
    allow_reuse_address = True


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--db", required=True, help="path to sqlite db file")
    parser.add_argument("--port", type=int, default=6001, help="port to listen on")
    args = parser.parse_args()

    server = ThreadedTCPServer(("127.0.0.1", args.port), Handler)
    server.db_path = args.db
    server.db = None
    try:
        print(f"Mock backend listening on port {args.port}, db={args.db}", flush=True)
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        server.server_close()
        if server.db:
            server.db.close()


if __name__ == "__main__":
    main()
