package com.walletconnect.android.pulse.domain

import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.pulse.data.PulseService
import com.walletconnect.android.pulse.model.Event
import com.walletconnect.foundation.util.Logger
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

abstract class SendEventUseCase(
    private val pulseService: PulseService,
    private val logger: Logger,
    internal val bundleId: String
) {
    operator fun invoke(event: Event) {
        scope.launch {
            supervisorScope {
                try {
                    val response = pulseService.sendEvent(body = event)
                    if (!response.isSuccessful) {
                        logger.error("kobe: Failed to send event: ${event.props.type}")
                    } else {
                        logger.log("kobe: Event sent successfully: ${event.props.type}")
                    }
                } catch (e: Exception) {
                    logger.error("kobe: Failed to send event: ${event.props.type}, error: $e")
                }
            }
        }
    }
}