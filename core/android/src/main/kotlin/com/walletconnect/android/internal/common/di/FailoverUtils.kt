package com.walletconnect.android.internal.common.di

import android.net.Uri
import java.io.IOException
import java.net.SocketException

internal const val DEFAULT_RELAY_URL: String = "wss://relay.walletconnect.com"
internal const val FAIL_OVER_RELAY_URL: String = "wss://relay.walletconnect.org"

internal var ECHO_URL: String = "https://echo.walletconnect.com/"
internal const val DEFAULT_ECHO_URL: String = "https://echo.walletconnect.com/"
internal const val FAIL_OVER_ECHO_URL: String = "https://echo.walletconnect.org"

internal var VERIFY_URL: String = "https://verify.walletconnect.com/"
internal const val DEFAULT_VERIFY_URL: String = "https://verify.walletconnect.com/"
internal const val FAIL_OVER_VERIFY_URL: String = "https://verify.walletconnect.org"

internal var wasRelayFailOvered = false
internal var wasEchoFailOvered = false
internal var wasVerifyFailOvered = false

internal fun shouldFallbackRelay(host: String): Boolean = wasRelayFailOvered && host == DEFAULT_RELAY_URL.host
internal fun shouldFallbackEcho(host: String): Boolean = wasEchoFailOvered && host == DEFAULT_ECHO_URL.host
internal fun shouldFallbackVerify(host: String): Boolean = wasVerifyFailOvered && host == DEFAULT_VERIFY_URL.host
internal fun getPathAndQuery(url: String) = with(Uri.parse(url)) { return@with Pair(this.path, this.query) }
internal fun isFailOverException(e: Exception) = (e is SocketException || e is IOException)
internal val String.host: String? get() = Uri.parse(this).host