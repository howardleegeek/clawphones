import pytest

from clawphones.api import api, APIError, InMemoryAPI


@pytest.fixture(autouse=True)
def run_around_tests():
    # Reset in-memory storage before every test to ensure isolation
    api._store.clear()
    api._next_id = 1
    yield
    # No special teardown


def test_create_user_valid():
    data = {"username": "alice", "email": "alice@example.com", "age": 30}
    user = api.create_user(data, task_id="t1")
    assert user["id"] == 1
    assert user["username"] == "alice"
    assert user["email"] == "alice@example.com"
    assert user["age"] == 30


def test_create_user_missing_fields_raises():
    data = {"username": "bob", "email": "bob@example.com"}
    with pytest.raises(APIError) as exc:
        api.create_user(data, task_id="t2")
    assert exc.value.status_code == 400
    assert "Missing required fields" in exc.value.message


def test_get_user_existing():
    api.create_user({"username": "carol", "email": "carol@example.com", "age": 25}, task_id="t3")
    user = api.get_user(1, task_id="t3a")
    assert user["username"] == "carol"


def test_get_user_invalid_id_raises():
    with pytest.raises(APIError) as exc:
        api.get_user(-5, task_id="t4")
    assert exc.value.status_code == 400


def test_update_user_valid():
    api.create_user({"username": "dave", "email": "dave@example.com", "age": 40}, task_id="t5")
    updated = api.update_user(1, {"age": 41}, task_id="t5b")
    assert updated["age"] == 41


def test_update_user_invalid_field_raises():
    api.create_user({"username": "eve", "email": "eve@example.com", "age": 22}, task_id="t6")
    with pytest.raises(APIError) as exc:
        api.update_user(1, {"nickname": "evie"}, task_id="t6b")
    assert exc.value.status_code == 400


def test_get_user_not_found_raises():
    with pytest.raises(APIError) as exc:
        api.get_user(999, task_id="t7")
    assert exc.value.status_code == 404
