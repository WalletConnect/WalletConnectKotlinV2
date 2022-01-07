package com.walletconnect.walletconnectv2.common.model.errors

import java.net.HttpURLConnection

val Throwable.exception: Throwable
    get() =
        when {
            this.message?.contains(HttpURLConnection.HTTP_UNAUTHORIZED.toString()) == true ->
                WalletConnectExceptions.ProjectIdDoesNotExistException(this.message)
            this.message?.contains(HttpURLConnection.HTTP_FORBIDDEN.toString()) == true ->
                WalletConnectExceptions.InvalidProjectIdException(this.message)
            else -> WalletConnectExceptions.ServerException(this.message)
        }