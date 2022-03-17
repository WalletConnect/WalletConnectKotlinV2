package com.walletconnect.walletconnectv2.core.model.vo.sequence

import com.walletconnect.walletconnectv2.core.model.type.Sequence
import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.MetaDataVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.SessionParticipantVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.params.SessionParamsVO
import com.walletconnect.walletconnectv2.engine.model.EngineDO

internal data class SessionVO(
    override val topic: TopicVO,
    override val expiry: ExpiryVO,
    val relayProtocol: String,
    val relayData: String?,
    val controllerKey: PublicKey? = null,
    val selfParticipant: PublicKey,
    val selfMetaData: MetaDataVO? = null,
    val peerParticipant: PublicKey? = null,
    val peerMetaData: MetaDataVO? = null,
    val accounts: List<String> = emptyList(),
    val chains: List<String>,
    val methods: List<String>,
    val types: List<String>?,
    val isAcknowledged: Boolean,
) : Sequence {
    val isPeerController: Boolean = peerParticipant?.keyAsHex == controllerKey?.keyAsHex
    val isSelfController: Boolean = selfParticipant.keyAsHex == controllerKey?.keyAsHex

    internal companion object {

        @JvmSynthetic
        internal fun createUnacknowledgedSession(
            sessionTopic: TopicVO,
            proposal: EngineDO.SessionProposal,
            selfParticipant: SessionParticipantVO,
            sessionExpiry: Long,
        ): SessionVO {
            val peerMetaData = MetaDataVO(proposal.name, proposal.description, proposal.url, proposal.icons.map { it.toString() })
            return SessionVO(
                sessionTopic,
                ExpiryVO(sessionExpiry),
                relayProtocol = proposal.relayProtocol,
                relayData = proposal.relayData,
                peerParticipant = PublicKey(proposal.proposerPublicKey),
                peerMetaData = peerMetaData,
                selfParticipant = PublicKey(selfParticipant.publicKey),
                selfMetaData = selfParticipant.metadata,
                controllerKey = PublicKey(selfParticipant.publicKey),
                chains = proposal.chains,
                methods = proposal.methods,
                types = proposal.types ?: emptyList(),
                accounts = proposal.accounts,
                isAcknowledged = false
            )
        }

        @JvmSynthetic
        internal fun createAcknowledgedSession(
            sessionTopic: TopicVO,
            settleParams: SessionParamsVO.SessionSettleParams,
            selfPublicKey: PublicKey,
            selfMetadata: MetaDataVO,
        ): SessionVO {
            return SessionVO(
                sessionTopic,
                ExpiryVO(settleParams.expiryTimestamp),
                relayProtocol = settleParams.relay.protocol,
                relayData = settleParams.relay.data,
                peerParticipant = PublicKey(settleParams.controller.publicKey),
                peerMetaData = settleParams.controller.metadata,
                selfParticipant = selfPublicKey,
                selfMetaData = selfMetadata,
                controllerKey = PublicKey(settleParams.controller.publicKey),
                chains = settleParams.blockchain.chains,
                methods = settleParams.permission.jsonRpc.methods,
                types = settleParams.permission.notifications?.types,
                accounts = settleParams.blockchain.accounts,
                isAcknowledged = true
            )
        }
    }
}