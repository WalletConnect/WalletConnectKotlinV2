@file:JvmSynthetic

package com.walletconnect.android.utils

import android.net.Uri
import com.walletconnect.android.internal.common.exception.*
import com.walletconnect.android.relay.ConnectionType
import java.net.HttpURLConnection

@JvmSynthetic
internal fun String.strippedUrl() = Uri.parse(this).run {
    this@run.scheme + "://" + this@run.authority
}

@JvmSynthetic
internal fun ConnectionType.toCommonConnectionType(): ConnectionType =
    when (this) {
        ConnectionType.AUTOMATIC -> ConnectionType.AUTOMATIC
        ConnectionType.MANUAL -> ConnectionType.MANUAL
    }

@JvmSynthetic
internal fun String.isValidRelayServerUrl(): Boolean {
    return this.isNotBlank() && Uri.parse(this)?.let { relayUrl ->
        arrayOf("wss", "ws").contains(relayUrl.scheme) && !relayUrl.getQueryParameter("projectId").isNullOrBlank()
    } ?: false
}

// Assumes isValidRelayServerUrl returns true.
@JvmSynthetic
internal fun String.projectId(): String {
    return Uri.parse(this)!!.let { relayUrl ->
         relayUrl.getQueryParameter("projectId")!!
    }
}

@get:JvmSynthetic
internal val Throwable.toWalletConnectException: WalletConnectException
    get() =
        when {
            this.message?.contains(HttpURLConnection.HTTP_UNAUTHORIZED.toString()) == true ->
                UnableToConnectToWebsocketException("${this.message}. It's possible that JWT has expired. Try initializing the CoreClient again.")
            this.message?.contains(HttpURLConnection.HTTP_NOT_FOUND.toString()) == true ->
                ProjectIdDoesNotExistException(this.message)
            this.message?.contains(HttpURLConnection.HTTP_FORBIDDEN.toString()) == true ->
                InvalidProjectIdException(this.message)
            else -> GenericException("Error while connecting, please check your Internet connection or contact support: $this")
        }

@get:JvmSynthetic
val Int.Companion.DefaultId
    get() = -1
