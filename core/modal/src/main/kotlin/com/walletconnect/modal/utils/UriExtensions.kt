package com.walletconnect.modal.utils

import androidx.compose.ui.platform.UriHandler
import timber.log.Timber
import java.net.URLEncoder

private const val WC_URI_QUERY = "wc?uri="

fun UriHandler.goToNativeWallet(uri: String, nativeLink: String?) {
    try {
        nativeLink?.let { openUri(formatNativeDeeplink(it, uri)) } ?: Timber.e("Invalid native link")
    } catch (e: Exception) {
        Timber.e(e)
    }
}

fun UriHandler.goToUniversalLink(uri: String, universalLink: String?) {
    try {
        universalLink?.let { openUri(formatUniversalLink(it, uri)) } ?: Timber.e("Invalid universal link")
    } catch (e: Exception) {
        Timber.e(e)
    }
}

fun UriHandler.goToPlayStore(playStoreLink: String) {
    try {
        openUri(playStoreLink)
    } catch (e: Exception) {
        Timber.e(e)
    }
}

private fun formatNativeDeeplink(nativeLink: String, uri: String): String = nativeLink + WC_URI_QUERY + uri.encodeUri()

private fun formatUniversalLink(universalLink: String, uri: String): String {
    val plainAppUrl = if (universalLink.endsWith('/')) {
        universalLink.dropLast(1)
    } else {
        universalLink
    }
    return plainAppUrl + "/" + WC_URI_QUERY + uri.encodeUri()
}

private fun String.encodeUri() = URLEncoder.encode(this, "UTF-8")

fun UriHandler.openPlayStore(playStoreLink: String?) {
    playStoreLink?.let { openUri(it) } ?: Timber.e("Invalid play store link")
}
