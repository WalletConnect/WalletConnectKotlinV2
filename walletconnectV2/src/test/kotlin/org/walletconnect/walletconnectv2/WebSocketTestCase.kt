package org.walletconnect.walletconnectv2

import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.io.IOException

open class WebSocketTestCase(private val autoStartServer: Boolean = true) {
    protected val mockWebServer = MockWebServer()

    @BeforeEach
    fun startServer() {
        if (autoStartServer) {
            mockWebServer.start()
        }
    }

    @AfterEach
    fun stopServer() {
        try {
            mockWebServer.shutdown()
        } catch (exc: IOException) {
            // Ignore the weird "Gave up waiting for queue to shut down" exception in
            // MockWebServer when the code under test closes the connection explicitly
            // TODO: Raise issue in OkHTTP repo
        }
    }
}