package com.walletconnect.android.pulse.domain.w3m

import com.walletconnect.android.internal.utils.currentTimeInSeconds
import com.walletconnect.android.pulse.data.PulseService
import com.walletconnect.android.pulse.domain.SendEventUseCase
import com.walletconnect.android.pulse.model.Event
import com.walletconnect.android.pulse.model.SDKType
import com.walletconnect.android.pulse.model.properties.Props
import com.walletconnect.android.pulse.model.properties.SelectWalletProperties
import com.walletconnect.foundation.util.Logger
import com.walletconnect.util.generateId

class SendSelectWalletUseCase(
    pulseService: PulseService,
    logger: Logger,
    bundleId: String
) : SendEventUseCase(pulseService, logger, bundleId) {
    operator fun invoke(name: String, platform: String) {
        val properties = SelectWalletProperties(name, platform)
        super.invoke(
            Event(
                eventId = generateId(),
                bundleId = bundleId,
                timestamp = currentTimeInSeconds,
                props = Props.W3M.SelectWallet(properties = properties)
            ), SDKType.WEB3MODAL
        )
    }
}