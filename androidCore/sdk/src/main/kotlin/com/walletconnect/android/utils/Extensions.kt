@file:JvmSynthetic

package com.walletconnect.android.utils

import android.net.Uri
import android.os.Build
import com.walletconnect.android.internal.common.exception.WalletConnectException
import com.walletconnect.android.connection.ConnectionType
import com.walletconnect.android.exception.GenericException
import com.walletconnect.android.exception.InvalidProjectIdException
import com.walletconnect.android.exception.ProjectIdDoesNotExistException
import java.net.HttpURLConnection

@JvmSynthetic
internal fun String.strippedUrl() = Uri.parse(this).run {
    this@run.scheme + "://" + this@run.authority
}

@JvmSynthetic
internal fun String.addUserAgent(sdkVersion: String): String {
    return Uri.parse(this).buildUpon()
        // TODO: Setup env variable for version and tag. Use env variable here instead of hard coded version
        .appendQueryParameter("ua", """wc-2/kotlin-$sdkVersion/android-${Build.VERSION.RELEASE}""")
        .build()
        .toString()
}

@JvmSynthetic
internal fun ConnectionType.toCommonConnectionType(): com.walletconnect.android.internal.common.connection.ConnectionType =
    when (this) {
        ConnectionType.AUTOMATIC -> com.walletconnect.android.internal.common.connection.ConnectionType.AUTOMATIC
        ConnectionType.MANUAL -> com.walletconnect.android.internal.common.connection.ConnectionType.MANUAL
    }

@JvmSynthetic
internal fun String.isValidRelayServerUrl(): Boolean {
    return this.isNotBlank() && Uri.parse(this)?.let { relayUrl ->
        arrayOf("wss", "ws").contains(relayUrl.scheme) && !relayUrl.getQueryParameter("projectId").isNullOrBlank()
    } ?: false
}

@get:JvmSynthetic
internal val Throwable.toWalletConnectException: WalletConnectException
    get() =
        when {
            this.message?.contains(HttpURLConnection.HTTP_UNAUTHORIZED.toString()) == true ->
                ProjectIdDoesNotExistException(this.message)
            this.message?.contains(HttpURLConnection.HTTP_FORBIDDEN.toString()) == true ->
                InvalidProjectIdException(this.message)
            else -> GenericException(this.message)
        }

@get:JvmSynthetic
val Int.Companion.DefaultId
    get() = -1
