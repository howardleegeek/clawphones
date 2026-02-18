import http.server
import ssl
import threading
import json
import logging
from typing import Optional

# Lightweight HTTPS test server used by S01-016 task.
# Exposes /health endpoint and returns simple JSON.

def _config_logger(task_id: Optional[str]) -> logging.Logger:
    logger = logging.getLogger("https-test-server")
    if not logger.handlers:
        handler = logging.StreamHandler()
        formatter = logging.Formatter("[%(levelname)s] %(message)s")
        handler.setFormatter(formatter)
        logger.addHandler(handler)
        logger.setLevel(logging.INFO)
    if task_id:
        logger = logging.LoggerAdapter(logger, {"task_id": task_id})  # type: ignore
    return logger  # type: ignore


class _HealthHandler(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path != "/health":
            self.send_response(404)
            self.end_headers()
            return
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        self.wfile.write(b"{\"status\":\"ok\"}")

    def log_message(self, format, *args):  # pragma: no cover - quiet HTTP logs
        return


class HttpsServerWrapper:
    def __init__(self, host: str, port: int, certfile: str, keyfile: str, task_id: Optional[str] = None):
        self.host = host
        self.port = port
        self.certfile = certfile
        self.keyfile = keyfile
        self.task_id = task_id
        self._httpd = None
        self._thread = None
        self._logger = _config_logger(task_id)

    def start(self) -> None:
        self._httpd = http.server.HTTPServer((self.host, self.port), _HealthHandler)
        ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
        ssl_context.load_cert_chain(certfile=self.certfile, keyfile=self.keyfile)
        self._httpd.socket = ssl_context.wrap_socket(self._httpd.socket, server_side=True)
        self.port = self._httpd.server_address[1]
        self._thread = threading.Thread(target=self._httpd.serve_forever, daemon=True)
        self._thread.start()
        self._logger.info("HTTPS server started on %s:%d", self.host, self.port)

    def stop(self) -> None:
        if self._httpd:
            self._httpd.shutdown()
            self._httpd.server_close()
            self._logger.info("HTTPS server stopped (task_id=%s)", self.task_id)
        if self._thread:
            self._thread.join(timeout=1)


def create_https_server(certfile: str, keyfile: str, host: str = "127.0.0.1", port: int = 0, task_id: str | None = None) -> HttpsServerWrapper:
    """Create and start a small HTTPS server for health checks.

    Returns the wrapper which exposes the bound port (since port may be 0).
    """
    server = HttpsServerWrapper(host, port, certfile, keyfile, task_id)
    server.start()
    return server
