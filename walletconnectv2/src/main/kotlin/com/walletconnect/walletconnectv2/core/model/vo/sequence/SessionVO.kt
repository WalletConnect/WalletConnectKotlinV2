package com.walletconnect.walletconnectv2.core.model.vo.sequence

import com.walletconnect.walletconnectv2.core.model.type.Sequence
import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.AppMetaDataVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.SessionParticipantVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.params.SessionParamsVO
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.util.Time

internal data class SessionVO(
    override val topic: TopicVO,
    override val expiry: ExpiryVO,
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
    val isAcknowledged: Boolean,
) : Sequence {
    val isPeerController: Boolean = peerParticipant?.keyAsHex == controllerKey?.keyAsHex
    val isSelfController: Boolean = selfParticipant.keyAsHex == controllerKey?.keyAsHex

    internal companion object {

        private val sessionExpirySeconds: Long get() = Time.currentTimeInSeconds + Time.weekInSeconds

        @JvmSynthetic
        internal fun createUnacknowledgedSession(
            settledTopic: TopicVO,
            proposal: EngineDO.SessionProposal,
            selfParticipant: SessionParticipantVO,
        ): SessionVO {
            val peerMetaData = AppMetaDataVO(proposal.name, proposal.description, proposal.url, proposal.icons.map { it.toString() })
            return SessionVO(
                settledTopic,
                ExpiryVO(sessionExpirySeconds),
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
            selfMetadata: AppMetaDataVO,
        ): SessionVO {
            return SessionVO(
                sessionTopic,
                ExpiryVO(sessionExpirySeconds),
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