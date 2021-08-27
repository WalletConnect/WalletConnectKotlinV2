package org.walletconnect.walletconnectv2

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.*
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.walletconnect.walletconnectv2.util.changeEngineToMock
import org.walletconnect.walletconnectv2.util.setupWebSocketListener
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class DefaultHttpClientTest {
    private val spyHttpClient: HttpClient = spyk(HttpClient(OkHttp) {
        install(WebSockets)

        engine {
            config {
                pingInterval(40, TimeUnit.SECONDS)
                callTimeout(5, TimeUnit.MINUTES)
                connectTimeout(5, TimeUnit.MINUTES)
                readTimeout(5, TimeUnit.MINUTES)
                writeTimeout(5, TimeUnit.MINUTES)
            }
        }
    })

    @Nested
    inner class LocalClient {

        @Test
        fun `Host name should be the localhost IP address`() {
            val sut = buildClient()

            assertEquals("127.0.0.1", sut.hostName)
        }

        @Test
        fun `Port should default to 276`() {
            val sut = buildClient()

            assertEquals(DefaultHttpClient.defaultLocalPort, sut.port)
        }

        @Test
        fun `Port should be overridable`() {
            val customPort = Random(0).nextInt()
            val sut = buildClient(port = customPort)

            assertEquals(customPort, sut.port)
        }
    }

    @InternalAPI
    @Test
    fun `OkHTTP should be the client engine`() {
        val sut = buildClient()

        assertTrue(sut.client.engine is OkHttpEngine)
    }

    @Nested
    inner class WebSocketConnection: WebSocketTestCase() {

        @Test
        fun `Open a non-secure WebSocket connection and send a message to WebSocket`() {
            val sampleMessage = "This is a sample message"
            val sut = buildClient(false, port = mockWebServer.port).apply {
                changeEngineToMock()
            }

            mockWebServer.setupWebSocketListener(
                onMessage = { _, text ->
                    assertEquals(sampleMessage, text)
                }
            )

            runBlocking {
                sut.wsConnect() {
                    send(sampleMessage)
                }
            }
        }
    }

    @Test
    fun `Close properly closes Ktor client`() {
        val sut = buildClient()

        sut.close()

        verify {
            sut.client.close()
        }
    }

    private fun buildClient(useTLs: Boolean = false, port: Int = DefaultHttpClient.defaultLocalPort, httpClient: HttpClient = spyHttpClient): DefaultHttpClient =
        DefaultHttpClient.initLocal(useTLs, port).apply { client = httpClient }

    private suspend fun DefaultClientWebSocketSession.outputMessages() {
        try {
            for (message in incoming) {
                message as? Frame.Text ?: continue
                println(message.readText())
            }

        } catch (e: Exception) {
            println("Error while receiving: " + e.localizedMessage)
        }
    }

    private suspend fun DefaultClientWebSocketSession.inputMessages() {
        while (true) {
            val message = readLine() ?: ""
            if (message.equals("exit", true)) return
            try {
                send(message)
            } catch (e: Exception) {
                println("Error while sending: " + e.localizedMessage)
                return
            }
        }
    }
}