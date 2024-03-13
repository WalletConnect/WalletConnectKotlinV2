package com.walletconnect.android.pulse.domain

import com.walletconnect.android.internal.utils.currentTimeInSeconds
import com.walletconnect.android.pulse.data.PulseService
import com.walletconnect.android.pulse.model.Event
import com.walletconnect.android.pulse.model.properties.ConnectSuccessProperties
import com.walletconnect.android.pulse.model.properties.Props
import com.walletconnect.foundation.util.Logger
import com.walletconnect.util.generateId

class SendConnectSuccessUseCase(
    pulseService: PulseService,
    logger: Logger,
    bundleId: String
) : SendEventUseCase(pulseService, logger, bundleId) {
    operator fun invoke(name: String, method: String) {
        val properties = ConnectSuccessProperties(name, method)
        super.invoke(
            Event(
                eventId = generateId(),
                bundleId = bundleId,
                timestamp = currentTimeInSeconds,
                props = Props.ConnectSuccess(properties = properties)
            )
        )
    }
}