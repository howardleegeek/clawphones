import os
import json
import hashlib
import logging


def _default_mock_transcription(audio_bytes: bytes, language_code: str) -> str:
    """A tiny deterministic mock for speech-to-text.

    This avoids external dependencies in tests while giving predictable results
    based on the input bytes content.
    """
    if not audio_bytes:
        return ""
    upper = audio_bytes.upper()
    if b"HELLO" in upper:
        return "hello world"
    if b"TEST_A" in audio_bytes:
        return "transcribed A"
    # Fallback generic transcription
    return f"transcript[{language_code}]:{len(audio_bytes)}bytes"


class AIIntegration:
    """Lightweight AI voice transcription integration with caching.

    This class provides a transcribe(audio_bytes, language_code) API. It will:
    - cache transcripts on disk based on hash of the input bytes
    - count real API calls (simulated here by a mock function)
    - include task_id in log records for traceability
    """

    def __init__(self, task_id: str = "default_task", storage_dir: str = "ai_cache", use_real_api: bool = False):
        self.task_id = task_id
        self.storage_dir = storage_dir
        self.use_real_api = use_real_api
        self._api_call_count = 0
        self._ensure_dir()

        # Setup a per-task logger so tests can attach and verify context
        self._logger = logging.getLogger(f"AIIntegration.{self.task_id}")
        self._logger.setLevel(logging.INFO)
        if not self._logger.handlers:
            self._logger.addHandler(logging.StreamHandler())

    def _ensure_dir(self):
        try:
            os.makedirs(self.storage_dir, exist_ok=True)
        except Exception:
            # Non-fatal; transcripts can still be kept in-memory if filesystem is read-only
            pass

    def _hash_audio(self, audio_bytes: bytes) -> str:
        return hashlib.sha256(audio_bytes).hexdigest()

    def _cache_path(self, key: str) -> str:
        return os.path.join(self.storage_dir, f"{key}.txt")

    def _log(self, event: str, key: str, language_code: str):
        self._logger.info("AIIntegration event=%s key=%s lang=%s task_id=%s", event, key, language_code, self.task_id)

    def _call_speech_to_text(self, audio_bytes: bytes, language_code: str) -> str:
        self._api_call_count += 1
        if self.use_real_api:
            # In a real environment, integrate with Google Speech-to-Text here.
            # For testability in CI, fall back to mock.
            return _default_mock_transcription(audio_bytes, language_code)
        return _default_mock_transcription(audio_bytes, language_code)

    def transcribe(self, audio_bytes: bytes, language_code: str = "en-US") -> str:
        key = self._hash_audio(audio_bytes)
        cache_file = self._cache_path(key)
        if os.path.exists(cache_file):
            transcript = None
            try:
                with open(cache_file, "r", encoding="utf-8") as f:
                    transcript = f.read()
            except Exception:
                transcript = None
            self._log("cache_hit", key, language_code)
            if transcript is not None:
                return transcript
        self._log("api_call_start", key, language_code)
        transcript = self._call_speech_to_text(audio_bytes, language_code)
        try:
            with open(cache_file, "w", encoding="utf-8") as f:
                f.write(transcript)
        except Exception:
            # If cache write fails, still return transcript
            pass
        self._log("api_call_end", key, language_code)
        return transcript

    def get_api_call_count(self) -> int:
        return self._api_call_count

    def clear_cache(self) -> None:
        try:
            for fname in os.listdir(self.storage_dir):
                if fname.endswith(".txt"):
                    os.remove(os.path.join(self.storage_dir, fname))
        except Exception:
            pass
