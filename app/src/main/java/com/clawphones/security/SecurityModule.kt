package com.clawphones.security

/** Simple security module that encrypts sensitive data in-memory and logs actions. */
class SecurityModule(
    private val keyProvider: () -> String,
    private val logger: Logger = NoOpLogger(),
    private val taskIdSupplier: () -> String = { System.getProperty("task_id") ?: "unknown" }
) {

    private val store = HashMap<String, String>()

    /** Stores a value encrypted under the provided key. */
    fun storeSensitive(key: String, value: String) {
        val aes = AesUtil(keyProvider())
        val encrypted = aes.encrypt(value.toByteArray(Charsets.UTF_8))
        store[key] = encrypted
        log("Stored encrypted value for key '$key'")
    }

    /** Retrieves and decrypts the value for the given key. */
    fun retrieveSensitive(key: String): String {
        val encrypted = store[key] ?: run {
            log("Attempted to retrieve non-existent key '$key'")
            throw IllegalArgumentException("No value stored for key: $key")
        }
        val aes = AesUtil(keyProvider())
        val decrypted = aes.decrypt(encrypted)
        log("Retrieved decrypted value for key '$key'")
        return String(decrypted, Charsets.UTF_8)
    }

    private fun log(message: String) {
        logger.log("task_id=${taskIdSupplier()}", message)
    }
}

interface Logger {
    fun log(context: String, message: String)
}

class NoOpLogger : Logger {
    override fun log(context: String, message: String) {
        // no-op
    }
}
