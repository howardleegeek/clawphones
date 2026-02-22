import time
import logging
from typing import Any, Callable, Optional


class NetworkTimeout(Exception):
    """Exception representing a simulated network timeout."""


class LLMProxy:
    """A tiny proxy that retries a request to an LLM provider on timeout.

    The proxy does not perform real I/O itself; it relies on a caller-provided
    function (call_fn) to simulate the provider behavior. This makes it
    testable in isolation.
    """

    def __init__(self, max_retries: int = 2, backoff_seconds: float = 0.01, task_id: Optional[str] = None):
        self.max_retries = max_retries
        self.backoff_seconds = backoff_seconds
        self.logger = logging.getLogger(__name__)
        self.task_id = task_id

    def _log(self, message: str) -> None:
        if self.task_id:
            self.logger.info("[task_id=%s] %s", self.task_id, message)
        else:
            self.logger.info(message)

    def request(self, payload: Any, call_fn: Callable[[Any, int], Any], timeout: Optional[float] = None) -> Any:
        """Execute call_fn with retry on NetworkTimeout.

        - payload: data passed to the provider function
        - call_fn: a function(payload, attempt) -> Any that may raise NetworkTimeout
        - timeout: kept for compatibility; tests simulate timeout via exception
        Returns the result of call_fn on success.
        Raises NetworkTimeout after exhausting all retries.
        """
        attempt = 0
        while True:
            try:
                self._log(f"Attempt {attempt + 1} calling provider with payload: {payload}")
                result = call_fn(payload, attempt)
                self._log("Provider response received")
                return result
            except NetworkTimeout:
                self._log(f"Timeout on attempt {attempt + 1}")
                if attempt >= self.max_retries:
                    self._log("Exceeded maximum retries; raising timeout")
                    raise
                self._log(f"Retrying after backoff: {self.backoff_seconds}s")
                time.sleep(self.backoff_seconds)
                attempt += 1
