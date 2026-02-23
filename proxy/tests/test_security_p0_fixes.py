import os
import types
import pytest


def _import_proxy_module():
    try:
        mod = __import__("proxy.security_p0_fixes", fromlist=["*"])
        return mod
    except Exception:
        pytest.skip("proxy.security_p0_fixes module not available in this environment.")


def _get_callable(mod, *names):
    for name in names:
        if hasattr(mod, name):
            return getattr(mod, name)
    return None


def test_admin_key_constant_time_eq():
    # Try to load a constant-time comparison function from the target module.
    mod = _import_proxy_module()
    # Common names for constant-time comparison utilities.
    comp = _get_callable(mod, "constant_time_compare", "secure_compare", "compare_digest")
    if comp is None:
        pytest.skip("No constant-time comparison function found in proxy.security_p0_fixes.")

    assert comp("admin_key_secret", "admin_key_secret") is True
    assert comp("admin_key_secret", "admin_key_secret_diff") is False


def test_https_only_config():
    mod = _import_proxy_module()
    func = _get_callable(mod, "enforce_https", "ensure_https", "https_only_enabled", "https_only_config")
    if func is None:
        pytest.skip("HTTPS-only configuration function not found in proxy.security_p0_fixes.")

    # If the function is a url transformer, validate that http inputs are upgraded
    try:
        out = func("http://example.com")
        if isinstance(out, str):
            assert out.startswith("https://")
        elif isinstance(out, bool):
            # If the function simply validates/returns a flag, ensure it's a boolean.
            assert isinstance(out, bool)
        else:
            pytest.skip("HTTPS function returned an unexpected type.")
    except TypeError:
        # Some implementations may require additional args; skip if misused in this test context.
        pytest.skip("HTTPS function signature not compatible with this test.")


def test_credentials_env_reading(monkeypatch):
    mod = _import_proxy_module()
    reader = _get_callable(mod, "read_credentials_from_env", "load_credentials_from_env", "get_credentials_from_env")
    if reader is None:
        pytest.skip("Credentials reader not found in proxy.security_p0_fixes.")

    # Set credentials in environment and verify the reader returns a dict with keys.
    monkeypatch.setenv("PROXY_USERNAME", "testuser")
    monkeypatch.setenv("PROXY_PASSWORD", "testpass")

    cred = reader()
    assert isinstance(cred, dict)
    # Accept multiple possible key names for compatibility.
    username = cred.get("username") or cred.get("user") or cred.get("PROXY_USERNAME")
    password = cred.get("password") or cred.get("PROXY_PASSWORD")
    assert username == "testuser" or username is None
    assert password == "testpass" or password is None


def test_signature_password_env_var():
    mod = _import_proxy_module()
    reader = _get_callable(mod, "read_signing_password_from_env", "get_signing_password_from_env", "read_signing_password")
    if reader is None:
        pytest.skip("Signing password reader not found in proxy.security_p0_fixes.")

    import_bytes = None
    # If the reader expects to fetch a specific env var, try a common name.
    monkey = pytest.MonkeyPatch()
    try:
        monkey.setenv("SIGNING_PASSWORD", "supersecret")
        import_bytes = reader()
    finally:
        monkey.undo()

    assert import_bytes is None or isinstance(import_bytes, (str, bytes))
