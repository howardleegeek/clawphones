import os
import sqlite3
import tempfile
import multiprocessing
import time


def setup_db(db_path: str):
    os.makedirs(os.path.dirname(db_path), exist_ok=True)
    conn = sqlite3.connect(db_path, timeout=30)
    c = conn.cursor()
    # Enable WAL for better concurrency in tests
    c.execute("PRAGMA journal_mode=WAL;")
    c.execute("PRAGMA synchronous=NORMAL;")
    c.execute(
        """
        CREATE TABLE IF NOT EXISTS items (
            id INTEGER PRIMARY KEY,
            worker TEXT,
            value TEXT
        )
        """
    )
    conn.commit()
    conn.close()


def insert_range(db_path: str, start_id: int, count: int, worker_name: str):
    import sqlite3
    conn = sqlite3.connect(db_path, timeout=30, check_same_thread=False)
    c = conn.cursor()
    for i in range(count):
        success = False
        for _ in range(5):
            try:
                c.execute("INSERT INTO items (id, worker, value) VALUES (?, ?, ?)", (start_id + i, worker_name, f"{worker_name}-{start_id + i}"))
                conn.commit()
                success = True
                break
            except sqlite3.OperationalError as e:
                if "locked" in str(e):
                    time.sleep(0.05)
                else:
                    raise
        if not success:
            raise RuntimeError("Insertion failed due to lock")
    conn.close()


def test_concurrent_writes_basic():
    with tempfile.TemporaryDirectory() as d:
        db_path = os.path.join(d, "test.db")
        setup_db(db_path)

        num_workers = 4
        count_per_worker = 200

        procs = []
        for w in range(num_workers):
            p = multiprocessing.Process(target=insert_range, args=(db_path, w * count_per_worker + 1, count_per_worker, f"w{w}"))
            p.start()
            procs.append(p)
        for p in procs:
            p.join()

        conn = sqlite3.connect(db_path)
        c = conn.cursor()
        c.execute("SELECT COUNT(*) FROM items")
        total = c.fetchone()[0]
        conn.close()
        assert total == num_workers * count_per_worker

        # Basic integrity: ensure IDs cover the full expected range without gaps beyond those created by transactions
        conn = sqlite3.connect(db_path)
        c = conn.cursor()
        c.execute("SELECT MIN(id), MAX(id) FROM items")
        min_id, max_id = c.fetchone()
        conn.close()
        assert min_id == 1
        assert max_id == num_workers * count_per_worker


def test_transaction_commit_and_rollback():
    with tempfile.TemporaryDirectory() as d:
        db_path = os.path.join(d, "test.db")
        setup_db(db_path)

        # Commit path
        conn = sqlite3.connect(db_path)
        c = conn.cursor()
        c.execute("BEGIN")
        for i in range(1, 51):
            c.execute("INSERT INTO items (id, worker, value) VALUES (?, ?, ?)", (i, "txn", f"val-{i}"))
        conn.commit()
        c.execute("SELECT COUNT(*) FROM items")
        committed = c.fetchone()[0]
        conn.close()
        assert committed == 50

        # Rollback path
        conn = sqlite3.connect(db_path)
        c = conn.cursor()
        c.execute("BEGIN")
        try:
            for i in range(51, 71):
                c.execute("INSERT INTO items (id, worker, value) VALUES (?, ?, ?)", (i, "txn", f"val-{i}"))
            # Force a rollback
            raise RuntimeError("force rollback")
            conn.commit()
        except Exception:
            conn.rollback()
        c.execute("SELECT COUNT(*) FROM items WHERE id >= 51 AND id <= 70")
        after_rollback = c.fetchone()[0]
        conn.close()
        assert after_rollback == 0


def test_no_data_loss_under_concurrency():
    with tempfile.TemporaryDirectory() as d:
        db_path = os.path.join(d, "test.db")
        setup_db(db_path)

        num_workers = 3
        count_per_worker = 300
        procs = []
        for w in range(num_workers):
            p = multiprocessing.Process(target=insert_range, args=(db_path, w * count_per_worker + 1, count_per_worker, f"w{w}"))
            p.start()
            procs.append(p)
        for p in procs:
            p.join()

        conn = sqlite3.connect(db_path)
        c = conn.cursor()
        c.execute("SELECT COUNT(*) FROM items")
        total = c.fetchone()[0]
        c.execute("SELECT COUNT(DISTINCT id) FROM items")
        distinct = c.fetchone()[0]
        conn.close()
        assert total == num_workers * count_per_worker
        assert distinct == total
