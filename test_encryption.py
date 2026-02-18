import pytest
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC
from cryptography.hazmat.primitives import hashes
import os
import base64


class TestRepository:
    
    @pytest.fixture(autouse=True)
    def setup(self):
        self.salt = os.urandom(16)
        self.password = "testPassword123"
        self.key = self._derive_key(self.password, self.salt)
        self.aesgcm = AESGCM(self.key)
    
    def _derive_key(self, password: str, salt: bytes) -> bytes:
        kdf = PBKDF2HMAC(
            algorithm=hashes.SHA256(),
            length=32,
            salt=salt,
            iterations=65536,
        )
        return kdf.derive(password.encode())
    
    def _encrypt(self, plaintext: str) -> str:
        iv = os.urandom(12)
        ciphertext = self.aesgcm.encrypt(iv, plaintext.encode(), None)
        result = iv + ciphertext
        return base64.b64encode(result).decode()
    
    def _decrypt(self, encrypted: str) -> str:
        data = base64.b64decode(encrypted)
        iv = data[:12]
        ciphertext = data[12:]
        return self.aesgcm.decrypt(iv, ciphertext, None).decode()
    
    def test_encrypt_and_decrypt_returns_original_value(self):
        original_text = "Hello, World!"
        encrypted = self._encrypt(original_text)
        decrypted = self._decrypt(encrypted)
        assert decrypted == original_text
    
    def test_encrypt_produces_different_output_each_call(self):
        original_text = "Sensitive Data"
        encrypted1 = self._encrypt(original_text)
        encrypted2 = self._encrypt(original_text)
        assert encrypted1 != encrypted2
    
    def test_encrypt_large_data_handles_correctly(self):
        large_text = "A" * 10000
        encrypted = self._encrypt(large_text)
        decrypted = self._decrypt(encrypted)
        assert decrypted == large_text
    
    def test_encrypt_special_characters_handles_correctly(self):
        special_text = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~\x00\x01"
        encrypted = self._encrypt(special_text)
        decrypted = self._decrypt(encrypted)
        assert decrypted == special_text
    
    def test_encrypt_unicode_characters_handles_correctly(self):
        unicode_text = "你好世界🌍🎉日本語"
        encrypted = self._encrypt(unicode_text)
        decrypted = self._decrypt(encrypted)
        assert decrypted == unicode_text
    
    def test_encrypt_empty_string_handles_correctly(self):
        empty_text = ""
        encrypted = self._encrypt(empty_text)
        decrypted = self._decrypt(encrypted)
        assert decrypted == empty_text


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
