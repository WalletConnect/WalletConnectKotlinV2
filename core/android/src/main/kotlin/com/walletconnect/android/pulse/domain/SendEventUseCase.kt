package com.walletconnect.android.pulse.domain

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.pulse.data.PulseService
import com.walletconnect.android.pulse.model.Event
import com.walletconnect.foundation.util.Logger
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.koin.core.qualifier.named

abstract class SendEventUseCase(
    private val pulseService: PulseService,
    private val logger: Logger,
    internal val bundleId: String
) {
    private val enableAnalytics: Boolean by lazy { wcKoinApp.koin.get(named(AndroidCommonDITags.ENABLE_ANALYTICS)) }

    operator fun invoke(event: Event) {
        if (enableAnalytics) {
            scope.launch {
                supervisorScope {
                    try {
                        val response = pulseService.sendEvent(body = event)
                        if (!response.isSuccessful) {
                            logger.error("Failed to send event: ${event.props.type}")
                        } else {
                            logger.log("Event sent successfully: ${event.props.type}")
                        }
                    } catch (e: Exception) {
                        logger.error("Failed to send event: ${event.props.type}, error: $e")
                    }
                }
            }
        }
    }
}