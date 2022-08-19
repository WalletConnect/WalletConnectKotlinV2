package com.walletconnect.auth.common

import com.walletconnect.android_core.common.model.Expiry
import com.walletconnect.android_core.common.model.MetaData
import com.walletconnect.android_core.common.model.RelayProtocolOptions
import com.walletconnect.android_core.utils.ACTIVE_PAIRING
import com.walletconnect.android_core.utils.INACTIVE_PAIRING
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.android_core.common.model.type.Sequence
import com.walletconnect.auth.engine.model.EngineDO
import com.walletconnect.auth.engine.model.toAbsoluteString

internal data class PairingVO(
    override val topic: Topic,
    override val expiry: Expiry,
    val peerMetaData: MetaData? = null,
    val relayProtocol: String,
    val relayData: String?,
    val uri: String,
    val isActive: Boolean,
) : Sequence {

    companion object {

        @JvmSynthetic
        internal fun createInactivePairing(topic: Topic, relay: RelayProtocolOptions, uri: String): PairingVO {
            return PairingVO(
                topic,
                Expiry(INACTIVE_PAIRING),
                uri = uri,
                relayProtocol = relay.protocol,
                relayData = relay.data,
                isActive = false
            )
        }

        @JvmSynthetic
        internal fun createActivePairing(uri: EngineDO.WalletConnectUri): PairingVO {
            return PairingVO(
                uri.topic,
                Expiry(ACTIVE_PAIRING),
                uri = uri.toAbsoluteString(),
                relayProtocol = uri.relay.protocol,
                relayData = uri.relay.data,
                isActive = true
            )
        }
    }
}
