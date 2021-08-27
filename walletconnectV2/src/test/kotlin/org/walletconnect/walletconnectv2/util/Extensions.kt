package org.walletconnect.walletconnectv2.util

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy

inline fun MockWebServer.setupWebSocketListener(
    socketPolicy: SocketPolicy? = null,
    crossinline onOpen: (WebSocket, Response) -> Unit = { _, _ -> },
    crossinline onMessage: (WebSocket, String) -> Unit = { _, _ -> },
    crossinline onClosing: (WebSocket, Int, String) -> Unit = { _, _, _ -> },
    crossinline onClosed: (WebSocket, Int, String) -> Unit = { _, _, _ -> },
    crossinline onFailure: (WebSocket, Throwable, Response?) -> Unit = { _, _, _ -> }
) {
    enqueue(
        MockResponse()
            .withWebSocketUpgrade(
                object: WebSocketListener() {

                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        onOpen(webSocket, response)
                        super.onOpen(webSocket, response)
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        onMessage(webSocket, text)
                        super.onMessage(webSocket, text)
                    }

                    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                        onClosing(webSocket, code, reason)
                        super.onClosing(webSocket, code, reason)
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        onClosed(webSocket, code, reason)
                        super.onClosed(webSocket, code, reason)
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        onFailure(webSocket, t, response)
                        super.onFailure(webSocket, t, response)
                    }
                }
            ).apply {
                if (socketPolicy != null) {
                    setSocketPolicy(socketPolicy)
                }
            }
    )
}