package com.walletconnect.android.pulse.domain.w3m

import com.walletconnect.android.internal.utils.currentTimeInSeconds
import com.walletconnect.android.pulse.data.PulseService
import com.walletconnect.android.pulse.domain.SendEventUseCase
import com.walletconnect.android.pulse.model.Event
import com.walletconnect.android.pulse.model.SDKType
import com.walletconnect.android.pulse.model.properties.ConnectErrorProperties
import com.walletconnect.android.pulse.model.properties.Props
import com.walletconnect.foundation.util.Logger
import com.walletconnect.util.generateId

class SendConnectErrorUseCase(
    pulseService: PulseService,
    logger: Logger,
    bundleId: String
) : SendEventUseCase(pulseService, logger, bundleId) {
    operator fun invoke(message: String) {
        val properties = ConnectErrorProperties(message)
        super.invoke(
            Event(
                eventId = generateId(),
                bundleId = bundleId,
                timestamp = currentTimeInSeconds,
                props = Props.W3M.ConnectError(properties = properties)
            ), SDKType.WEB3MODAL
        )
    }
}