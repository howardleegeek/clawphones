package com.clawphones.data.network

import com.sun.net.httpserver.HttpServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.OutputStream
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

class ApiServiceTest {
    private lateinit var server: HttpServer
    private var port: Int = 0

    @After
    fun tearDown() {
        if (::server.isInitialized) {
            server.stop(0)
        }
    }

    @Test
    fun testUpdateUserSuccess() {
        startServer { exchange ->
            val method = exchange.requestMethod
            val path = exchange.requestURI.path
            if (method == "PUT" && path == "/users/123") {
                val resp = """
                    {"success": true, "user": { "id": "123", "name": "Alice", "email": "alice@example.com", "phone": "111-222" }}
                """.trimIndent()
                exchange.sendResponseHeaders(200, resp.toByteArray(StandardCharsets.UTF_8).size.toLong())
                val os: OutputStream = exchange.responseBody
                os.write(resp.toByteArray(StandardCharsets.UTF_8))
                os.close()
            } else {
                val resp = "{\"success\":false}"
                exchange.sendResponseHeaders(404, resp.toByteArray(StandardCharsets.UTF_8).size.toLong())
                exchange.responseBody.write(resp.toByteArray(StandardCharsets.UTF_8))
                exchange.close()
            }
        }
        val baseUrl = "http://127.0.0.1:${port}/"
        val service = HttpApiService(baseUrl)
        val req = UserUpdateRequest(name = "Alice")
        val resp = service.updateUser("123", req)
        assertTrue(resp.success)
        assertNotNull(resp.user)
        assertEquals("123", resp.user?.id)
        assertEquals("Alice", resp.user?.name)
    }

    @Test(expected = NetworkException::class)
    fun testUpdateUserFailure() {
        startServer { exchange ->
            val resp = "{\"error\": \"boom\"}"
            exchange.sendResponseHeaders(500, resp.toByteArray(StandardCharsets.UTF_8).size.toLong())
            exchange.responseBody.write(resp.toByteArray(StandardCharsets.UTF_8))
            exchange.close()
        }
        val baseUrl = "http://127.0.0.1:${port}/"
        val service = HttpApiService(baseUrl)
        val req = UserUpdateRequest(name = "Bob")
        service.updateUser("99", req)
    }

    private fun startServer(setup: (com.sun.net.httpserver.HttpExchange) -> Unit) {
        server = HttpServer.create(InetSocketAddress(0), 0)
        port = server.address.port
        server.createContext("/", { exchange -> setup(exchange) })
        server.start()
    }
}
