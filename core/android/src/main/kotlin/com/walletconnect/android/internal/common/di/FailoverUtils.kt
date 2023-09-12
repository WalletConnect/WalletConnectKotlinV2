package com.walletconnect.android.internal.common.di

import android.net.Uri
import androidx.core.net.toUri
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
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

internal fun shouldFallbackRelay(host: String): Boolean = wasRelayFailOvered && host == DEFAULT_RELAY_URL.toUri().host
internal fun shouldFallbackEcho(host: String): Boolean = wasEchoFailOvered && host == DEFAULT_ECHO_URL.toUri().host
internal fun shouldFallbackVerify(host: String): Boolean = wasVerifyFailOvered && host == DEFAULT_VERIFY_URL.toUri().host
internal fun getFallbackEchoUrl(url: String): String = with(Uri.parse(url)) {
    val (path, query) = Pair(this.path, this.query)
    return@with "$FAIL_OVER_ECHO_URL$path?$query}"
}

internal fun getFallbackVerifyUrl(url: String): String = "$FAIL_OVER_VERIFY_URL/attestation/${Uri.parse(url).lastPathSegment}"

internal fun isFailOverException(e: Exception) = (e is SocketException || e is IOException)

internal fun fallbackEcho(request: Request, chain: Interceptor.Chain): Response {
    ECHO_URL = FAIL_OVER_ECHO_URL
    wasEchoFailOvered = true
    return chain.proceed(request.newBuilder().url(getFallbackEchoUrl(request.url.toString())).build())
}

internal fun fallbackVerify(request: Request, chain: Interceptor.Chain): Response {
    VERIFY_URL = FAIL_OVER_VERIFY_URL
    wasVerifyFailOvered = true
    return chain.proceed(request.newBuilder().url(getFallbackVerifyUrl(request.url.toString())).build())
}

internal fun Scope.fallbackRelay(request: Request, chain: Interceptor.Chain): Response {
    SERVER_URL = "$FAIL_OVER_RELAY_URL?projectId=${Uri.parse(SERVER_URL).getQueryParameter("projectId")}"
    wasRelayFailOvered = true
    return chain.proceed(request.newBuilder().url(get<String>(named(AndroidCommonDITags.RELAY_URL))).build())
}