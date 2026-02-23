import pytest

from android_framework import AndroidFramework


def test_device_capabilities():
    af = AndroidFramework(task_id="t1")
    caps = af.get_device_capabilities()
    assert isinstance(caps, dict)
    assert "has_npu" in caps and "memory_mb" in caps


def test_load_and_already_loaded():
    af = AndroidFramework(task_id="t2")
    res = af.load_model("modelA")
    assert res["status"] == "loaded" and res["model"] == "modelA"
    res2 = af.load_model("modelA")
    assert res2["status"] == "already_loaded"


def test_unload_model():
    af = AndroidFramework(task_id="t3")
    af.load_model("m1")
    res = af.unload_model("m1")
    assert res["status"] == "unloaded"
    res2 = af.unload_model("m1")
    assert res2["status"] == "not_loaded"


def test_inference():
    af = AndroidFramework(task_id="t4")
    res = af.run_inference("classifier", {"x": 1})
    assert res["status"] == "ok"
    assert res["output"]["model"] == "classifier"
    assert isinstance(res["output"]["predictions"], list)
    assert res["output"]["input_seen"] == {"x": 1}


def test_inference_validation():
    af = AndroidFramework(task_id="t5")
    with pytest.raises(ValueError):
        af.run_inference("", {"a": 1})
    # Pass a non-dict to trigger validation; ignore type hints for test
    with pytest.raises(ValueError):
        af.run_inference("m", None)  # type: ignore[arg-type]


def test_batch_inference():
    af = AndroidFramework(task_id="t6")
    inputs = [{"a": 1}, {"b": 2}]
    res = af.run_batch_inference("classifier", inputs)
    assert res["status"] == "ok"
    assert res["batch_size"] == 2
    assert len(res["results"]) == 2
    assert res["results"][0]["index"] == 0
    assert res["results"][1]["index"] == 1


def test_inference_count():
    af = AndroidFramework(task_id="t7")
    af.run_inference("m", {"x": 1})
    af.run_inference("m", {"x": 2})
    assert af.get_inference_count() == 2


def test_initialize():
    af = AndroidFramework(task_id="t8")
    res = af.initialize()
    assert res["status"] == "initialized"
    assert res["platform"] == "Android"
