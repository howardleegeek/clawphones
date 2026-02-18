import pytest

import logging
import os
import sys

# Ensure top-level module import works when tests run from different CWDs
repo_root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
if repo_root not in sys.path:
    sys.path.insert(0, repo_root)

from android_framework import AndroidFramework


def test_initialize_sets_platform_and_task():
    fw = AndroidFramework("R20260216-14")
    res = fw.initialize()
    assert res["status"] == "initialized"
    assert res["platform"] == "Android"


def test_run_inference_valid():
    fw = AndroidFramework("R20260216-14")
    out = fw.run_inference("binary", {"image": "data"})
    assert out["status"] == "ok"
    assert out["output"]["model"] == "binary"


def test_run_inference_invalid_model():
    fw = AndroidFramework("R20260216-14")
    with pytest.raises(ValueError):
        fw.run_inference("", {"a": 1})


def test_logging_includes_task_id(caplog):
    caplog.set_level(logging.INFO)
    fw = AndroidFramework("R20260216-14")
    fw.initialize()
    assert any(getattr(rec, "task_id", None) == "R20260216-14" for rec in caplog.records)


def test_get_device_capabilities():
    fw = AndroidFramework("R20260216-14")
    caps = fw.get_device_capabilities()
    assert caps["has_npu"] is True
    assert caps["has_gpu"] is True
    assert caps["has_dsp"] is True
    assert caps["memory_mb"] == 8192
    assert caps["cpu_cores"] == 8


def test_load_model():
    fw = AndroidFramework("R20260216-14")
    result = fw.load_model("mobilebert")
    assert result["status"] == "loaded"
    assert "mobilebert" in fw.get_loaded_models()


def test_load_duplicate_model():
    fw = AndroidFramework("R20260216-14")
    fw.load_model("mobilebert")
    result = fw.load_model("mobilebert")
    assert result["status"] == "already_loaded"


def test_unload_model():
    fw = AndroidFramework("R20260216-14")
    fw.load_model("mobilebert")
    result = fw.unload_model("mobilebert")
    assert result["status"] == "unloaded"
    assert "mobilebert" not in fw.get_loaded_models()


def test_unload_not_loaded_model():
    fw = AndroidFramework("R20260216-14")
    result = fw.unload_model("nonexistent")
    assert result["status"] == "not_loaded"


def test_load_model_invalid_name():
    fw = AndroidFramework("R20260216-14")
    with pytest.raises(ValueError):
        fw.load_model("")


def test_detect_hardware_acceleration():
    fw = AndroidFramework("R20260216-14")
    result = fw.detect_hardware_acceleration()
    assert "npu" in result["accelerators"]
    assert "gpu" in result["accelerators"]
    assert result["available"] is True


def test_run_batch_inference():
    fw = AndroidFramework("R20260216-14")
    inputs = [{"image": "img1"}, {"image": "img2"}, {"image": "img3"}]
    result = fw.run_batch_inference("binary", inputs)
    assert result["status"] == "ok"
    assert result["batch_size"] == 3
    assert len(result["results"]) == 3
    assert result["results"][0]["index"] == 0


def test_run_batch_inference_invalid_model():
    fw = AndroidFramework("R20260216-14")
    with pytest.raises(ValueError):
        fw.run_batch_inference("", [{"a": 1}])


def test_run_batch_inference_invalid_inputs():
    fw = AndroidFramework("R20260216-14")
    with pytest.raises(ValueError):
        fw.run_batch_inference("binary", "not a list")


def test_inference_count():
    fw = AndroidFramework("R20260216-14")
    assert fw.get_inference_count() == 0
    fw.run_inference("binary", {"data": "x"})
    assert fw.get_inference_count() == 1
    fw.run_batch_inference("binary", [{"a": 1}, {"b": 2}])
    assert fw.get_inference_count() == 3
