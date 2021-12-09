package org.walletconnect.walletconnectv2.errors

import java.net.HttpURLConnection

val Throwable.exception: Throwable
    get() =
        when {
            this.message?.contains(HttpURLConnection.HTTP_UNAUTHORIZED.toString()) == true ->
                WalletConnectExceptions.ApiKeyDoesNotExistException(this.message)
            this.message?.contains(HttpURLConnection.HTTP_FORBIDDEN.toString()) == true ->
                WalletConnectExceptions.InvalidApiKeyException(this.message)
            else -> WalletConnectExceptions.ServerException(this.message)
        }