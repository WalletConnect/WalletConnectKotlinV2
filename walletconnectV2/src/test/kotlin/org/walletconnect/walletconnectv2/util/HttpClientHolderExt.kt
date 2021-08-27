package org.walletconnect.walletconnectv2.util

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import org.walletconnect.walletconnectv2.DefaultHttpClient

fun DefaultHttpClient.changeEngineToMock(useTLs: Boolean = false, port: Int = DefaultHttpClient.defaultLocalPort) {
    val scheme = if (useTLs) "wss" else "ws"
    client = HttpClient(MockEngine) {
        engine {
            addHandler { request ->
                when (request.url.fullUrl) {
                    "$scheme://127.0.0.1:$port/" -> {
                        respond("{\"name\":\"John\",\"age\":30,\"car\":null}",
                            HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        )
                    }
                    else -> error("Unknown server")
                }
            }
        }
    }
}

private val Url.hostWithPortIfRequired: String get() = if (port == protocol.defaultPort) host else hostWithPort
private val Url.fullUrl: String get() = "${protocol.name}://$hostWithPortIfRequired$fullPath"
