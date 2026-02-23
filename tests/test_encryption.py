import pytest
from Crypto.Cipher import AES
from Crypto.Random import get_random_bytes
import base64


class TestSecurityModuleEquivalent:
    
    def setup_method(self):
        self.key = get_random_bytes(32)
    
    def _encrypt(self, plain_text: str) -> str:
        if not plain_text:
            return ""
        cipher = AES.new(self.key, AES.MODE_GCM)
        cipher_text, tag = cipher.encrypt_and_digest(plain_text.encode('utf-8'))
        combined = cipher.nonce + cipher_text + tag
        return base64.b64encode(combined).decode('utf-8')
    
    def _decrypt(self, encrypted_text: str) -> str:
        if not encrypted_text:
            return ""
        combined = base64.b64decode(encrypted_text.encode('utf-8'))
        nonce = combined[:16]
        cipher_text = combined[16:-16]
        tag = combined[-16:]
        cipher = AES.new(self.key, AES.MODE_GCM, nonce=nonce)
        return cipher.decrypt_and_verify(cipher_text, tag).decode('utf-8')

    def test_encrypt_and_decrypt_basic_string(self):
        plain_text = "Hello, World!"
        encrypted = self._encrypt(plain_text)
        decrypted = self._decrypt(encrypted)
        assert decrypted == plain_text

    def test_encrypt_produces_different_output(self):
        plain_text = "SensitiveData123"
        encrypted1 = self._encrypt(plain_text)
        encrypted2 = self._encrypt(plain_text)
        assert encrypted1 != encrypted2

    def test_encrypt_empty_string(self):
        plain_text = ""
        encrypted = self._encrypt(plain_text)
        decrypted = self._decrypt(encrypted)
        assert decrypted == plain_text

    def test_encrypt_and_decrypt_auth_token(self):
        auth_token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.testtoken"
        encrypted = self._encrypt(auth_token)
        decrypted = self._decrypt(encrypted)
        assert decrypted == auth_token

    def test_encrypt_and_decrypt_api_key(self):
        api_key = "sk-1234567890abcdefghijklmnopqrstuvwxyz"
        encrypted = self._encrypt(api_key)
        decrypted = self._decrypt(encrypted)
        assert decrypted == api_key

    def test_encrypt_and_decrypt_unicode(self):
        plain_text = "用户数据🔐加密"
        encrypted = self._encrypt(plain_text)
        decrypted = self._decrypt(encrypted)
        assert decrypted == plain_text

    def test_encrypt_and_decrypt_long_string(self):
        plain_text = "A" * 10000
        encrypted = self._encrypt(plain_text)
        decrypted = self._decrypt(encrypted)
        assert decrypted == plain_text

    def test_encrypt_and_decrypt_special_characters(self):
        plain_text = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~"
        encrypted = self._encrypt(plain_text)
        decrypted = self._decrypt(encrypted)
        assert decrypted == plain_text


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
