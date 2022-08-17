@file:JvmSynthetic

package com.walletconnect.sign.common.model.vo.sequence

import com.walletconnect.android_core.common.model.Expiry
import com.walletconnect.android_core.utils.ACTIVE_PAIRING
import com.walletconnect.android_core.utils.INACTIVE_PAIRING
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.model.type.Sequence
import com.walletconnect.sign.common.model.vo.clientsync.common.MetaDataVO
import com.walletconnect.sign.common.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toAbsoluteString

internal data class PairingVO(
    override val topic: Topic,
    override val expiry: Expiry,
    val peerMetaData: MetaDataVO? = null,
    val relayProtocol: String,
    val relayData: String?,
    val uri: String,
    val isActive: Boolean,
) : Sequence {

    companion object {

        @JvmSynthetic
        internal fun createInactivePairing(topic: Topic, relay: RelayProtocolOptionsVO, uri: String): PairingVO {
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