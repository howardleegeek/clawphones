package com.clawphones.data

import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import org.junit.Test
import java.security.SecureRandom

class RepositoryTest {

    private lateinit var repository: Repository

    @Before
    fun setUp() {
        repository = Repository.getInstance()
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        repository.initialize("testPassword123", salt)
    }

    @After
    fun tearDown() {
        repository.clear()
        Repository.InMemoryStorage.clear()
    }

    @Test
    fun encryptAndDecrypt_string_returnsOriginalValue() {
        val originalText = "Hello, World!"
        val encrypted = repository.encrypt(originalText)
        val decrypted = repository.decrypt(encrypted)

        assertEquals(originalText, decrypted)
    }

    @Test
    fun encrypt_producesDifferentOutput_eachCall() {
        val originalText = "Sensitive Data"
        
        val encrypted1 = repository.encrypt(originalText)
        val encrypted2 = repository.encrypt(originalText)

        assertNotEquals(encrypted1, encrypted2)
    }

    @Test
    fun encryptAndDecrypt_byteArray_returnsOriginalData() {
        val originalData = "Binary data \u0000\u0001\u0002".toByteArray(Charsets.UTF_8)
        
        val encrypted = repository.encrypt(originalData)
        val decrypted = repository.decrypt(encrypted)

        assertArrayEquals(originalData, decrypted)
    }

    @Test
    fun saveAndLoad_storesEncryptedData() {
        val key = "userToken"
        val value = "secret-token-12345"

        repository.save(key, value)
        val loaded = repository.load(key)

        assertEquals(value, loaded)
    }

    @Test
    fun load_nonExistentKey_returnsNull() {
        val result = repository.load("nonExistentKey")
        assertNull(result)
    }

    @Test
    fun encrypt_largeData_handlesCorrectly() {
        val largeText = "A".repeat(10000)
        
        val encrypted = repository.encrypt(largeText)
        val decrypted = repository.decrypt(encrypted)

        assertEquals(largeText, decrypted)
    }

    @Test
    fun encrypt_specialCharacters_handlesCorrectly() {
        val specialText = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~\u0000\u0001"
        
        val encrypted = repository.encrypt(specialText)
        val decrypted = repository.decrypt(encrypted)

        assertEquals(specialText, decrypted)
    }

    @Test
    fun encrypt_unicodeCharacters_handlesCorrectly() {
        val unicodeText = "你好世界🌍🎉日本語"
        
        val encrypted = repository.encrypt(unicodeText)
        val decrypted = repository.decrypt(encrypted)

        assertEquals(unicodeText, decrypted)
    }

    @Test
    fun encrypt_emptyString_handlesCorrectly() {
        val emptyText = ""
        
        val encrypted = repository.encrypt(emptyText)
        val decrypted = repository.decrypt(encrypted)

        assertEquals(emptyText, decrypted)
    }

    @Test(expected = IllegalStateException::class)
    fun decrypt_uninitializedRepository_throwsException() {
        val uninitializedRepo = Repository.getInstance()
        uninitializedRepo.clear()
        
        uninitializedRepo.decrypt("someEncryptedData")
    }

    @Test
    fun save_multipleKeys_storesIndependently() {
        repository.save("key1", "value1")
        repository.save("key2", "value2")
        repository.save("key3", "value3")

        assertEquals("value1", repository.load("key1"))
        assertEquals("value2", repository.load("key2"))
        assertEquals("value3", repository.load("key3"))
    }
}
