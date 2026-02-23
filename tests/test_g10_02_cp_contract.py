from pathlib import Path


API_FILE = Path("app/src/main/java/com/clawphones/network/ApiService.kt")
TEST_FILE = Path("app/src/test/java/com/clawphones/network/ApiServiceTest.kt")


def test_retry_contract_and_coverage_present() -> None:
    api_source = API_FILE.read_text(encoding="utf-8")
    test_source = TEST_FILE.read_text(encoding="utf-8")

    assert "maxRetries: Int = 3" in api_source
    assert "retryDelayMillis: Long = 1_000L" in api_source
    assert "task_id=G10-02-CP" in api_source
    assert "fun retriesThreeTimesBeforeSuccess()" in test_source
    assert "fun throwsAfterMaxRetries()" in test_source
    assert "fun retriesOnHttpErrorResponse()" in test_source
