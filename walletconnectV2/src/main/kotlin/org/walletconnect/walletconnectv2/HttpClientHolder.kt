package org.walletconnect.walletconnectv2

import io.ktor.client.*

interface HttpClientHolder {
    var client: HttpClient
}