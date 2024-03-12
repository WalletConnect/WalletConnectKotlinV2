package com.walletconnect.android.internal.common.di

import android.net.Uri
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import java.io.IOException
import java.net.SocketException

internal const val DEFAULT_RELAY_URL: String = "wss://relay.walletconnect.com"
internal const val FAIL_OVER_RELAY_URL: String = "wss://relay.walletconnect.org"

internal var PUSH_URL: String = "https://echo.walletconnect.com/"
internal const val DEFAULT_PUSH_URL: String = "https://echo.walletconnect.com/"
internal const val FAIL_OVER_PUSH_URL: String = "https://echo.walletconnect.org"

internal var VERIFY_URL: String = "https://verify.walletconnect.com/"
internal const val DEFAULT_VERIFY_URL: String = "https://verify.walletconnect.com/"
internal const val FAIL_OVER_VERIFY_URL: String = "https://verify.walletconnect.org"

internal var PULSE_URL: String = "https://pulse.walletconnect.com"
internal const val DEFAULT_PULSE_URL: String = "https://pulse.walletconnect.com"
internal const val FAIL_OVER_PULSE_URL: String = "https://pulse.walletconnect.org"

internal var wasRelayFailOvered = false
internal var wasEchoFailOvered = false
internal var wasVerifyFailOvered = false
internal var wasPulseFailOvered = false

internal fun shouldFallbackRelay(host: String): Boolean = wasRelayFailOvered && host == DEFAULT_RELAY_URL.host
internal fun shouldFallbackPush(host: String): Boolean = wasEchoFailOvered && host == DEFAULT_PUSH_URL.host
internal fun shouldFallbackVerify(host: String): Boolean = wasVerifyFailOvered && host == DEFAULT_VERIFY_URL.host
internal fun shouldFallbackPulse(host: String): Boolean = wasPulseFailOvered && host == DEFAULT_PULSE_URL.host
internal fun getFallbackPushUrl(url: String): String = with(Uri.parse(url)) {
    val (path, query) = Pair(this.path, this.query)
    return@with "$FAIL_OVER_PUSH_URL$path?$query}"
}

internal fun getFallbackVerifyUrl(url: String): String = "$FAIL_OVER_VERIFY_URL/attestation/${Uri.parse(url).lastPathSegment}"

internal fun getFallbackPulseUrl(): String = "$FAIL_OVER_PULSE_URL/e"

internal fun isFailOverException(e: Exception) = (e is SocketException || e is IOException)
internal val String.host: String? get() = Uri.parse(this).host

internal fun fallbackPush(request: Request, chain: Interceptor.Chain): Response {
    PUSH_URL = FAIL_OVER_PUSH_URL
    wasEchoFailOvered = true
    return chain.proceed(request.newBuilder().url(getFallbackPushUrl(request.url.toString())).build())
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

internal fun fallbackPulse(request: Request, chain: Interceptor.Chain): Response {
    PULSE_URL = FAIL_OVER_PULSE_URL
    wasPulseFailOvered = true
    return chain.proceed(request.newBuilder().url(getFallbackPulseUrl()).build())
}