package com.walletconnect.android.pulse.domain

import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.events.EventsRepository
import com.walletconnect.android.pulse.model.properties.Props
import com.walletconnect.foundation.util.Logger
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class InsertEventUseCase(
    private val eventsRepository: EventsRepository,
    private val logger: Logger
) {
    operator fun invoke(props: Props) {
        scope.launch {
            supervisorScope {
                try {
                    eventsRepository.insertOrAbort(props)
                } catch (e: Exception) {
                    logger.error("Inserting event ${props.type} error: $e")
                }
            }
        }
    }
}