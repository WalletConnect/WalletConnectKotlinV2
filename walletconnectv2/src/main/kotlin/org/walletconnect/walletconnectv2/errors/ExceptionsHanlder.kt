package org.walletconnect.walletconnectv2.errors

import java.net.HttpURLConnection

val Throwable.exception: Throwable
    get() =
        when {
            this.message?.contains(HttpURLConnection.HTTP_UNAUTHORIZED.toString()) == true ->
                ApiKeyDoesNotExistException(this.message)
            this.message?.contains(HttpURLConnection.HTTP_FORBIDDEN.toString()) == true ->
                InvalidApiKeyException(this.message)
            else -> ServerException(this.message)
        }