package com.walletconnect.android.pulse.domain

import com.walletconnect.android.internal.common.model.TelemetryEnabled
import com.walletconnect.android.internal.common.storage.events.EventsRepository
import com.walletconnect.android.pulse.data.PulseService
import com.walletconnect.android.pulse.model.SDKType
import com.walletconnect.foundation.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SendBatchEventUseCase(
    private val pulseService: PulseService,
    private val eventsRepository: EventsRepository,
    private val telemetryEnabled: TelemetryEnabled,
    private val logger: Logger,
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        if (telemetryEnabled.value) {
            var continueProcessing = true
            while (continueProcessing) {
                val events = eventsRepository.getAllWithLimitAndOffset(LIMIT, 0)
                if (events.isNotEmpty()) {
                    try {
                        logger.log("Sending batch events: ${events.size}")
                        val response = pulseService.sendEventBatch(body = events, sdkType = SDKType.EVENTS.type)
                        if (response.isSuccessful) {
                            eventsRepository.deleteByIds(events.map { it.eventId })
                        } else {
                            logger.log("Failed to send events: ${events.size}")
                            continueProcessing = false
                        }
                    } catch (e: Exception) {
                        logger.error("Error sending batch events: ${e.message}")
                        continueProcessing = false
                    }
                } else {
                    continueProcessing = false
                }
            }
        } else {
            try {
                eventsRepository.deleteAll()
            } catch (e: Exception) {
                logger.error("Failed to delete events, error: $e")
            }
        }
    }

    companion object {
        private const val LIMIT = 500
    }
}