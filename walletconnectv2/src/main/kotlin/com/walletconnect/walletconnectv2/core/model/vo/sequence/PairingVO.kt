package com.walletconnect.walletconnectv2.core.model.vo.sequence

import com.walletconnect.walletconnectv2.core.model.type.Sequence
import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.AppMetaDataVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.engine.model.mapper.toAbsoluteString
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStatus
import com.walletconnect.walletconnectv2.util.Empty
import com.walletconnect.walletconnectv2.util.pendingSequenceExpirySeconds
import com.walletconnect.walletconnectv2.util.proposedPairingExpirySeconds

internal data class PairingVO(
    override val topic: TopicVO,
    override val expiry: ExpiryVO,
    override val status: SequenceStatus,
    val selfMetaData: AppMetaDataVO? = null,
    val peerMetaData: AppMetaDataVO? = null,
    val relayProtocol: String,
    val relayData: String?,
    val uri: String,
    val outcomeTopic: TopicVO = TopicVO(String.Empty),
) : Sequence {

    companion object {
        internal fun createPairing(topic: TopicVO, relay: RelayProtocolOptionsVO, uri: String): PairingVO {
            return PairingVO(
                topic,
                ExpiryVO(proposedPairingExpirySeconds()), //todo: change to 5mins?
                SequenceStatus.ACKNOWLEDGED, //todo: remove states from pairings?
                uri = uri,
                relayProtocol = relay.protocol,
                relayData = relay.data
            )
        }

        internal fun createFromUri(uri: EngineDO.WalletConnectUri): PairingVO {
            return PairingVO(
                uri.topic,
                ExpiryVO(pendingSequenceExpirySeconds()), //todo: change to 5mins?
                SequenceStatus.ACKNOWLEDGED, //todo: remove states from pairings?
                uri = uri.toAbsoluteString(),
                relayProtocol = uri.relay.protocol,
                relayData = uri.relay.data
            )
        }

        //@JvmSynthetic
//internal fun PairingVO.toAcknowledgedPairingVO(
//    settledTopic: TopicVO,
//    params: PairingParamsVO.ApproveParams,
//    controllerType: ControllerType
//): PairingVO =
//    PairingVO(
//        settledTopic,
//        params.expiry,
//        SequenceStatus.ACKNOWLEDGED,
//        selfParticipant,
//        PublicKey(params.responder.publicKey),
//        controllerKey = if (controllerType == ControllerType.CONTROLLER) selfParticipant else PublicKey(params.responder.publicKey),
//        uri,
//        permissions = permissions,
//        relay = relay,
//        controllerType = controllerType,
//        appMetaDataVO = params.state?.metadata
//    )
    }
}