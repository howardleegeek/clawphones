import time
import pytest

from proxy.client import LLMProxyClient


def test_network_timeout_and_retry_mechanism():
    # Simulate a provider that times out twice, then succeeds
    calls = {"count": 0}

    class FlakyProvider:
        def __call__(self, payload, timeout):
            calls["count"] += 1
            if calls["count"] <= 2:
                raise TimeoutError("simulated timeout")
            return {"status": "ok", "payload": payload}

    client = LLMProxyClient(provider=FlakyProvider(), task_id="byz-1")
    result = client.request(payload={"q": "hello"}, timeout=0.05, max_retries=3)
    assert result["status"] == "ok"
    assert calls["count"] == 3


def test_network_timeout_exhausted_raises_timeout():
    # Simulate a provider that always times out (with a tiny delay to avoid flakiness)
    class DelayedProvider:
        def __call__(self, payload, timeout):
            delay = timeout + 0.01
            time.sleep(delay)
            raise TimeoutError("delayed timeout")

    client = LLMProxyClient(provider=DelayedProvider(), task_id="byz-2")
    with pytest.raises(TimeoutError):
        client.request(payload={}, timeout=0.01, max_retries=2)
