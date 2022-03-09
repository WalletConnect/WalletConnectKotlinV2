package com.walletconnect.walletconnectv2.core.model.vo.sequence

import com.walletconnect.walletconnectv2.core.model.type.Sequence
import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.AppMetaDataVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.params.PairingParamsVO
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStatus
import com.walletconnect.walletconnectv2.util.Empty
import com.walletconnect.walletconnectv2.util.Time
import com.walletconnect.walletconnectv2.util.pendingSequenceExpirySeconds
import java.util.*

internal data class SessionVO(
    override val topic: TopicVO,
    override val expiry: ExpiryVO,
    override val status: SequenceStatus,
    val relayProtocol: String,
    val relayData: String?,
    val controllerKey: PublicKey? = null,
    val selfParticipant: PublicKey,
    val selfMetaData: AppMetaDataVO? = null,
    val peerParticipant: PublicKey? = null,
    val peerMetaData: AppMetaDataVO? = null,
    val accounts: List<String> = emptyList(),
    val chains: List<String>,
    val methods: List<String>,
    val types: List<String>?,
    val outcomeTopic: TopicVO = TopicVO(String.Empty)
) : Sequence {
    val isPeerController: Boolean = peerParticipant?.keyAsHex == controllerKey?.keyAsHex
    val isSelfController: Boolean = selfParticipant.keyAsHex == controllerKey?.keyAsHex

    internal companion object {
        internal fun createProposedSession(topic: TopicVO, params: PairingParamsVO.SessionProposeParams): SessionVO = with(params) {
            return SessionVO(
                topic,
                ExpiryVO(pendingSequenceExpirySeconds()),
                SequenceStatus.PROPOSED,
                relayProtocol = relays.first().protocol,
                relayData = relays.first().data,
                selfParticipant = PublicKey(proposer.publicKey),
                selfMetaData = proposer.metadata,
                chains = blockchainProposedVO.chains,
                methods = permissions.jsonRpc.methods,
                types = permissions.notifications?.types,
            )
        }

        internal fun createRespondedSession(
            selfPublicKey: PublicKey,
            settledTopic: TopicVO,
            proposal: EngineDO.SessionProposal,
        ): SessionVO = with(proposal) {
            return SessionVO(
                settledTopic, //todo: topic A?
                ExpiryVO(pendingSequenceExpirySeconds()),
                SequenceStatus.RESPONDED,
                relayProtocol = relayProtocol,
                relayData = relayData,
                selfParticipant = selfPublicKey,
                chains = chains,
                methods = methods,
                types = types,
                outcomeTopic = settledTopic //todo: topic B?
            )
        }

        @JvmSynthetic
        internal fun createPreSettledSession(
            settledTopic: TopicVO,
            selfPublicKey: PublicKey,
            proposal: EngineDO.SessionProposal,
        ): SessionVO = with(proposal) {
            return SessionVO(
                settledTopic,
                ExpiryVO((Calendar.getInstance().timeInMillis / 1000) + Time.dayInSeconds), //todo: add proper expiry
                SequenceStatus.PRE_SETTLED,
                relayProtocol = relayProtocol,
                relayData = relayData,
                selfParticipant = selfPublicKey,
                peerParticipant = PublicKey(publicKey),
                controllerKey = PublicKey(publicKey),//todo: which is controller key else selfPublicKey,
                chains = chains,
                methods = methods,
                types = types ?: emptyList(),
                accounts = accounts
            )
        }
        //@JvmSynthetic
//internal fun SessionVO.toEngineDOAcknowledgeSessionVO(settledTopic: TopicVO, params: SessionParamsVO.ApprovalParams): SessionVO =
//    SessionVO(
//        settledTopic,
//        params.expiry,
//        SequenceStatus.ACKNOWLEDGED,
//        selfParticipant,
//        PublicKey(params.responder.publicKey),
//        controllerKey = PublicKey(params.responder.publicKey), //todo: should always set responder key as controller ?
//        metaData = params.responder.metadata,
//        relayProtocol = params.relay.protocol,
//        chains = chains,
//        methods = methods,
//        types = types,
//        accounts = params.state.accounts,
//        ttl = TtlVO(params.expiry.seconds)
//    )
    }
}