package com.walletconnect.web3.modal.utils

import androidx.compose.ui.platform.UriHandler
import com.walletconnect.web3.modal.domain.model.Wallet
import timber.log.Timber
import java.net.URLEncoder

private const val WC_URI_QUERY = "wc?uri="

internal fun UriHandler.goToNativeWallet(uri: String, wallet: Wallet) {
    try {
        when {
            !wallet.nativeLink.isNullOrBlank() -> openUri(formatNativeDeeplink(wallet.nativeLink, uri))
            !wallet.universalLink.isNullOrBlank() -> goToUniversalLink(uri, wallet)
            else -> goToPlayStore(wallet.playStoreLink)
        }
    } catch (e: Exception) {
        Timber.e(e)
        goToUniversalLink(uri, wallet)
    }
}

private fun UriHandler.goToUniversalLink(uri: String, wallet: Wallet) {
    try {
        when {
            !wallet.universalLink.isNullOrBlank() -> openUri(formatUniversalLink(wallet.universalLink, uri))
            else -> openUri(wallet.playStoreLink)
        }
    } catch (e: Exception) {
        Timber.e(e)
        goToPlayStore(wallet.playStoreLink)
    }
}

private fun UriHandler.goToPlayStore(playStoreLink: String) {
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
