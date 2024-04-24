package com.walletconnect.android.pulse.domain.w3m

import com.walletconnect.android.internal.utils.currentTimeInSeconds
import com.walletconnect.android.pulse.data.PulseService
import com.walletconnect.android.pulse.domain.SendEventUseCase
import com.walletconnect.android.pulse.model.Event
import com.walletconnect.android.pulse.model.SDKType
import com.walletconnect.android.pulse.model.properties.Props
import com.walletconnect.foundation.util.Logger
import com.walletconnect.util.generateId

class SendModalLoadedUseCase(
    pulseService: PulseService,
    logger: Logger,
    bundleId: String
) : SendModalLoadedUseCaseInterface, SendEventUseCase(pulseService, logger, bundleId) {

    override fun sendModalLoadedEvent() {
        super.invoke(
            Event(
                eventId = generateId(),
                bundleId = bundleId,
                timestamp = currentTimeInSeconds,
                props = Props.ModalLoaded()
            ), SDKType.WEB3MODAL
        )
    }
}

interface SendModalLoadedUseCaseInterface {
    fun sendModalLoadedEvent()
}