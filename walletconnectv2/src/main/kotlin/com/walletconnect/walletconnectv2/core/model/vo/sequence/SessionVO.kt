package com.walletconnect.walletconnectv2.core.model.vo.sequence

import com.walletconnect.walletconnectv2.core.model.type.Sequence
import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.MetaDataVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.SessionParticipantVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.params.PairingParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.params.SessionParamsVO
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.engine.model.mapper.toNamespacesVO

internal data class SessionVO(
    override val topic: TopicVO,
    override val expiry: ExpiryVO,
    val relayProtocol: String,
    val relayData: String?,
    val controllerKey: PublicKey? = null,
    val selfPublicKey: PublicKey,
    val selfMetaData: MetaDataVO? = null,
    val peerPublicKey: PublicKey? = null,
    val peerMetaData: MetaDataVO? = null,
    val accounts: List<String> = emptyList(),
    val namespaces: List<NamespaceVO>,
    val isAcknowledged: Boolean,
) : Sequence {
    val isPeerController: Boolean = peerPublicKey?.keyAsHex == controllerKey?.keyAsHex
    val isSelfController: Boolean = selfPublicKey.keyAsHex == controllerKey?.keyAsHex

    internal companion object {
        @JvmSynthetic
        internal fun createUnacknowledgedSession(
            sessionTopic: TopicVO,
            proposal: PairingParamsVO.SessionProposeParams,
            selfParticipant: SessionParticipantVO,
            sessionExpiry: Long,
            accounts: List<String>,
            namespaces: List<EngineDO.Namespace>,
        ): SessionVO {
            return SessionVO(
                sessionTopic,
                ExpiryVO(sessionExpiry),
                relayProtocol = proposal.relays.first().protocol,
                relayData = proposal.relays.first().data,
                peerPublicKey = PublicKey(proposal.proposer.publicKey),
                peerMetaData = proposal.proposer.metadata,
                selfPublicKey = PublicKey(selfParticipant.publicKey),
                selfMetaData = selfParticipant.metadata,
                controllerKey = PublicKey(selfParticipant.publicKey),
                accounts = accounts,
                namespaces = namespaces.toNamespacesVO(),
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
                ExpiryVO(settleParams.expiry),
                relayProtocol = settleParams.relay.protocol,
                relayData = settleParams.relay.data,
                peerPublicKey = PublicKey(settleParams.controller.publicKey),
                peerMetaData = settleParams.controller.metadata,
                selfPublicKey = selfPublicKey,
                selfMetaData = selfMetadata,
                controllerKey = PublicKey(settleParams.controller.publicKey),
                accounts = settleParams.accounts,
                namespaces = settleParams.namespaces,
                isAcknowledged = true
            )
        }

//        fun getChainIds(accountIds: List<String>): List<String> {
//            return accountIds.map { accountId -> accountId.split(":").take(2).joinToString(":") }
//        }
    }
}