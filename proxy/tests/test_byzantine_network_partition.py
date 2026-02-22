import pytest

from proxy.llm_proxy import LLMProxy, NetworkTimeout


def test_timeout_and_retry_mechanism_successful_after_retries():
    # Provider simulates a timeout on first two attempts, then succeeds on the third
    attempts = {"count": 0}

    def provider(payload, attempt):
        attempts["count"] += 1
        if attempt < 2:
            raise NetworkTimeout()
        return {"status": "ok", "payload": payload}

    proxy = LLMProxy(max_retries=2, backoff_seconds=0.001, task_id="test-1")
    result = proxy.request(payload={"q": "ping"}, call_fn=provider)

    assert result == {"status": "ok", "payload": {"q": "ping"}}
    # Should have been invoked 3 times: attempts 0, 1, 2
    assert attempts["count"] == 3


def test_timeout_and_retry_mechanism_exhausted_retries():
    # Provider always times out
    attempts = {"count": 0}

    def provider(payload, attempt):
        attempts["count"] += 1
        raise NetworkTimeout()

    proxy = LLMProxy(max_retries=2, backoff_seconds=0.001, task_id="test-2")

    with pytest.raises(NetworkTimeout):
        proxy.request(payload={"q": "ping"}, call_fn=provider)

    # Ensure it retried max_retries + 1 times
    assert attempts["count"] == 3
