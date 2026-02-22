import sqlite3
import threading
import time
import tempfile
import os


def setup_db(path: str) -> None:
    conn = sqlite3.connect(path, timeout=30.0)
    cur = conn.cursor()
    cur.execute("CREATE TABLE IF NOT EXISTS test_data (id INTEGER PRIMARY KEY AUTOINCREMENT, value TEXT)")
    conn.commit()
    conn.close()


def insert_batch(conn_path: str, thread_id: int, count: int, retry_wait: float = 0.05, max_retries: int = 20) -> None:
    for attempt in range(max_retries):
        conn = None
        try:
            conn = sqlite3.connect(conn_path, timeout=30.0)
            cur = conn.cursor()
            cur.execute("BEGIN")
            for i in range(count):
                cur.execute("INSERT INTO test_data (value) VALUES (?)", (f"t{thread_id}-r{i}",))
            conn.commit()
            return
        except sqlite3.OperationalError as e:
            if "database is locked" in str(e):
                time.sleep(retry_wait)
                continue
            raise
        finally:
            if conn:
                conn.close()
    raise RuntimeError(f"Thread {thread_id} max retries reached")


def test_concurrent_writes_sqlite() -> None:
    path = tempfile.NamedTemporaryFile(delete=False, suffix=".sqlite").name
    try:
        setup_db(path)
        NUM_THREADS = 8
        ROWS_PER_THREAD = 50
        threads = []
        for tid in range(NUM_THREADS):
            t = threading.Thread(target=insert_batch, args=(path, tid, ROWS_PER_THREAD))
            t.start()
            threads.append(t)
        for t in threads:
            t.join()

        conn = sqlite3.connect(path)
        total = conn.execute("SELECT COUNT(*) FROM test_data").fetchone()[0]
        conn.close()
        assert total == NUM_THREADS * ROWS_PER_THREAD
    finally:
        try:
            os.remove(path)
        except Exception:
            pass


def test_transaction_rollback_and_commit_integrity() -> None:
    path = tempfile.NamedTemporaryFile(delete=False, suffix=".sqlite").name
    try:
        setup_db(path)

        # Rollback path: ensure nothing is persisted
        conn = sqlite3.connect(path)
        cur = conn.cursor()
        cur.execute("BEGIN")
        cur.execute("INSERT INTO test_data (value) VALUES (?)", ("will-rollback-1",))
        cur.execute("INSERT INTO test_data (value) VALUES (?)", ("will-rollback-2",))
        conn.rollback()
        conn.close()

        conn = sqlite3.connect(path)
        count = conn.execute("SELECT COUNT(*) FROM test_data").fetchone()[0]
        conn.close()
        assert count == 0

        # Commit path: ensure data persists when committed
        conn = sqlite3.connect(path)
        cur = conn.cursor()
        cur.execute("BEGIN")
        cur.execute("INSERT INTO test_data (value) VALUES (?)", ("commit-1",))
        cur.execute("INSERT INTO test_data (value) VALUES (?)", ("commit-2",))
        cur.execute("INSERT INTO test_data (value) VALUES (?)", ("commit-3",))
        conn.commit()
        conn.close()

        conn = sqlite3.connect(path)
        committed_count = conn.execute("SELECT COUNT(*) FROM test_data").fetchone()[0]
        conn.close()
        assert committed_count == 3
    finally:
        try:
            os.remove(path)
        except Exception:
            pass
