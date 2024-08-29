@file:JvmSynthetic

package com.walletconnect.android.utils

import android.net.Uri
import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.exception.GenericException
import com.walletconnect.android.internal.common.exception.InvalidProjectIdException
import com.walletconnect.android.internal.common.exception.ProjectIdDoesNotExistException
import com.walletconnect.android.internal.common.exception.UnableToConnectToWebsocketException
import com.walletconnect.android.internal.common.exception.WalletConnectException
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.utils.Empty
import java.net.HttpURLConnection

@JvmSynthetic
internal fun String.strippedUrl() = Uri.parse(this).run {
    this@run.scheme + "://" + this@run.authority
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
                ProjectIdDoesNotExistException("Project ID doesn't exist: ${this.message}")

            this.message?.contains(HttpURLConnection.HTTP_FORBIDDEN.toString()) == true ->
                InvalidProjectIdException("Invalid project ID: ${this.message}")

            else -> GenericException("Error while connecting, please check your Internet connection or contact support: ${this.message}")
        }

@get:JvmSynthetic
val Int.Companion.DefaultId
    get() = -1

fun AppMetaData?.toClient() = Core.Model.AppMetaData(
    name = this?.name ?: String.Empty,
    description = this?.description ?: String.Empty,
    url = this?.url ?: String.Empty,
    icons = this?.icons ?: emptyList(),
    redirect = this?.redirect?.native,
    appLink = this?.redirect?.universal,
    linkMode = this?.redirect?.linkMode ?: false
)
