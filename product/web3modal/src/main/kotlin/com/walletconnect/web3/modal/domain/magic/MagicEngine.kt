package com.walletconnect.web3.modal.domain.magic

import android.content.Context
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.modal.domain.magic.model.MagicEvent
import com.walletconnect.web3.modal.domain.magic.model.MagicRequest
import com.walletconnect.web3.modal.ui.components.internal.email.webview.EmailMagicWebViewWrapper
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import org.koin.core.qualifier.named
import kotlin.coroutines.resume

internal class MagicEngine(
    context: Context,
    logger: Logger,
    appMetaData: AppMetaData,
    private val projectId: ProjectId,
) {

    internal var magicWebViewWrapper = EmailMagicWebViewWrapper(context, buildWebHeaders(), projectId, appMetaData, logger).apply {
        //todo refresh webview on activity change somehow
    }

    internal suspend inline fun<reified T : MagicEvent> sendMessage(message: MagicRequest): T {
        magicWebViewWrapper.sendMessage(message)
        return subscribeToEvent<T>()
    }

    private suspend inline fun <reified T : MagicEvent> subscribeToEvent(): T {
        return withTimeout(5000L) {
            suspendCancellableCoroutine { continuation ->
                val job = launch {
                    magicWebViewWrapper.eventFlow
                        .filterIsInstance<T>()
                        .collect { continuation.resume(it) }
                }
                continuation.invokeOnCancellation { job.cancel() }
            }
        }
    }


    // TODO: INJECT THIS BY KOIN
    // find way to inject version of BOM instead BuildConfig.SDK_VERSION, this return W3M version
    private fun buildWebHeaders() = mapOf(
        Pair("x-project-id", projectId.value),
        Pair("x-sdk-version", "kotlin-1.22.1"), // replace it with Version from android core
        Pair("x-sdk-type", "w3m"),
        Pair("user-agent", wcKoinApp.koin.get(named(AndroidCommonDITags.USER_AGENT)))
    )
}