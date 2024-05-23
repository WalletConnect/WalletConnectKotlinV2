package com.walletconnect.android.pulse.domain

import com.walletconnect.android.internal.common.model.TelemetryEnabled
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.events.EventsRepository
import com.walletconnect.android.pulse.data.PulseService
import com.walletconnect.foundation.util.Logger
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class SendBatchEventUseCase(
	private val pulseService: PulseService,
	private val eventsRepository: EventsRepository,
	private val telemetryEnabled: TelemetryEnabled,
	private val logger: Logger,
) {
	operator fun invoke() {
		if (telemetryEnabled.value) {
			scope.launch {
				supervisorScope {
//					try {
////						logger.log("Event: $event")
//						eventsRepository.getAll()
//						val response = pulseService.sendEventBatch(body = event, sdkType = SDKType.EVENTS.type)
//
//						if (!response.isSuccessful) {
//							logger.error("Failed to send event: ${event.props.type}")
//						} else {
//							logger.log("Event sent successfully: ${event.props.type}")
//						}
//					} catch (e: Exception) {
//						logger.error("Failed to send event: ${event.props.type}, error: $e")
//					}
				}
			}
		} else {
			println("kobe: //todo: remove all the events")
		}
	}
}