import os
import subprocess
import tempfile
import time
import ssl
import urllib.request

import pytest

from proxy.server import create_https_server


def _gen_self_signed_cert(tmpdir):
    cert_path = os.path.join(tmpdir, "cert.pem")
    key_path = os.path.join(tmpdir, "key.pem")
    # Generate a simple self-signed cert using OpenSSL (assumes OpenSSL is available in CI)
    subprocess.check_call([
        "openssl", "req", "-x509", "-nodes", "-days", "1",
        "-newkey", "rsa:2048",
        "-subj", "/CN=localhost",
        "-keyout", key_path,
        "-out", cert_path,
    ])
    return cert_path, key_path


def test_https_health_endpoint():
    with tempfile.TemporaryDirectory() as d:
        cert, key = _gen_self_signed_cert(d)
        server = create_https_server(cert, key, host="127.0.0.1", port=0, task_id="test-https")
        try:
            port = server.port
            url = f"https://127.0.0.1:{port}/health"
            ctx = ssl._create_unverified_context()
            with urllib.request.urlopen(url, context=ctx, timeout=5) as resp:
                assert resp.status == 200
                data = resp.read()
                assert b"status" in data
        finally:
            server.stop()
