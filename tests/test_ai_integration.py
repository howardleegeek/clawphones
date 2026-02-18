import os
import shutil
import logging

import pytest

from app.ai_integration import AIIntegration


CACHE_DIR = "ai_cache_test"


@pytest.fixture(autouse=True)
def run_around_tests():
    # Ensure clean slate before each test
    if os.path.isdir(CACHE_DIR):
        shutil.rmtree(CACHE_DIR)
    yield
    # Cleanup after tests
    if os.path.isdir(CACHE_DIR):
        shutil.rmtree(CACHE_DIR)


def _capture_logs(logger):
    records = []
    class ListHandler(logging.Handler):
        def emit(self, record):
            records.append(record)
    h = ListHandler()
    logger.addHandler(h)
    return records


def test_transcription_caching_and_logging():
    ai = AIIntegration(task_id="G13-02-CP", storage_dir=CACHE_DIR)
    audio = b"HELLO there"
    transcript1 = ai.transcribe(audio, "en-US")
    assert transcript1 == "hello world"
    assert ai.get_api_call_count() == 1

    # Capture logs for task_id in the message
    captured = []
    class ListHandler(logging.Handler):
        def emit(self, record):
            captured.append(record.getMessage())
    ai._logger.addHandler(ListHandler())
    transcript2 = ai.transcribe(audio, "en-US")
    assert transcript2 == transcript1
    # Ensure the task_id is present in logs at least once
    assert any("task_id=G13-02-CP" in msg for msg in captured)


def test_cache_file_written_for_transcript():
    ai = AIIntegration(task_id="G13-02-CP-cache", storage_dir=CACHE_DIR)
    audio = b"TEST_A data"
    transcript = ai.transcribe(audio, "en-US")
    assert transcript == "transcribed A"
    # Cache file should exist
    files = [f for f in os.listdir(CACHE_DIR) if f.endswith('.txt')]
    assert len(files) == 1
    with open(os.path.join(CACHE_DIR, files[0]), 'r', encoding='utf-8') as f:
        cached = f.read()
    assert cached == transcript


def test_empty_audio_transcription_behaves_consistently():
    ai = AIIntegration(task_id="G13-02-CP-empty", storage_dir=CACHE_DIR)
    transcript = ai.transcribe(b"", "en-US")
    assert transcript == ""
    files = [f for f in os.listdir(CACHE_DIR) if f.endswith('.txt')]
    assert len(files) == 1
    with open(os.path.join(CACHE_DIR, files[0]), 'r', encoding='utf-8') as f:
        cached = f.read()
    assert cached == transcript
