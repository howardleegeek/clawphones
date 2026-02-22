import time
import logging
from typing import Callable, Any, Optional


class LLMProxyClient:
    """A light-weight proxy client with retry-on-timeout behavior.

    The client delegates the actual network call to a provided provider callable.
    The provider must accept (payload, timeout) and may raise TimeoutError to
    indicate a timeout. The client will retry on TimeoutError up to max_retries
    with a small exponential backoff.
    """

    def __init__(self, provider: Callable[[Any, float], Any], task_id: Optional[str] = None,
                 logger: Optional[logging.Logger] = None):
        self.provider = provider
        self.task_id = task_id
        self.logger = logger or logging.getLogger(__name__)

    def _log(self, level: int, message: str) -> None:
        if self.task_id:
            self.logger.log(level, f"[task {self.task_id}] {message}")
        else:
            self.logger.log(level, message)

    def request(self, payload: Any, timeout: float = 1.0, max_retries: int = 3) -> Any:
        """Perform a request with retry on TimeoutError.

        Args:
            payload: Data to send to the provider.
            timeout: Per-attempt timeout in seconds (passed to provider).
            max_retries: How many attempts to make before giving up.
        Returns:
            The provider's return value on success.
        Raises:
            TimeoutError when all retries are exhausted.
        """
        attempt = 0
        while True:
            attempt += 1
            try:
                self._log(logging.DEBUG, f"Attempt {attempt} with timeout {timeout}s")
                result = self.provider(payload, timeout)
                self._log(logging.INFO, f"Request succeeded on attempt {attempt}")
                return result
            except TimeoutError as e:
                self._log(logging.WARNING, f"Timeout on attempt {attempt}/{max_retries}: {e}")
                if attempt >= max_retries:
                    self._log(logging.ERROR, "All retries exhausted; raising TimeoutError")
                    raise e
                # simple backoff between retries
                backoff = min(0.05 * (2 ** (attempt - 1)), 0.5)
                time.sleep(backoff)


def default_provider(payload: Any, timeout: float) -> Any:
    """A trivial default provider that sleeps up to timeout and returns a success payload."""
    sleep_time = min(timeout, 0.01)
    time.sleep(sleep_time)
    return {"status": "ok", "payload": payload}
