package com.walletconnect.walletconnectv2.core.model.vo.sequence

import com.walletconnect.walletconnectv2.core.model.type.Sequence
import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.MetaDataVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.engine.model.mapper.toAbsoluteString
import com.walletconnect.walletconnectv2.engine.model.mapper.toMetaDataVO
import com.walletconnect.walletconnectv2.util.Expiration

internal data class PairingVO(
    override val topic: TopicVO,
    override val expiry: ExpiryVO,
    val selfMetaData: MetaDataVO? = null,
    val peerMetaData: MetaDataVO? = null,
    val relayProtocol: String,
    val relayData: String?,
    val uri: String,
) : Sequence {

    companion object {

        internal fun createPairing(topic: TopicVO, relay: RelayProtocolOptionsVO, uri: String, metaData: EngineDO.AppMetaData): PairingVO {
            return PairingVO(
                topic,
                ExpiryVO(Expiration.inactivePairing),
                uri = uri,
                relayProtocol = relay.protocol,
                relayData = relay.data,
                peerMetaData = metaData.toMetaDataVO()
            )
        }

        internal fun createFromUri(uri: EngineDO.WalletConnectUri): PairingVO {
            return PairingVO(
                uri.topic,
                ExpiryVO(Expiration.activePairing),
                uri = uri.toAbsoluteString(),
                relayProtocol = uri.relay.protocol,
                relayData = uri.relay.data
            )
        }
    }
}