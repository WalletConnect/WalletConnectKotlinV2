@file:JvmSynthetic

package com.walletconnect.sign.common.model.vo.sequence

import com.walletconnect.android.api.Expiry
import com.walletconnect.android.impl.common.model.MetaData
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.android.impl.common.model.type.Sequence
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.common.model.vo.clientsync.common.SessionParticipantVO
import com.walletconnect.sign.common.model.vo.clientsync.pairing.params.PairingParamsVO
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SessionParamsVO
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toMapOfNamespacesVOSession

internal data class SessionVO(
    override val topic: Topic,
    override val expiry: Expiry,
    val relayProtocol: String,
    val relayData: String?,
    val controllerKey: PublicKey? = null,
    val selfPublicKey: PublicKey,
    val selfMetaData: MetaData? = null,
    val peerPublicKey: PublicKey? = null,
    val peerMetaData: MetaData? = null,
    val namespaces: Map<String, NamespaceVO.Session>,
    val proposalNamespaces: Map<String, NamespaceVO.Proposal>,
    val isAcknowledged: Boolean,
) : Sequence {
    val isPeerController: Boolean = peerPublicKey?.keyAsHex == controllerKey?.keyAsHex
    val isSelfController: Boolean = selfPublicKey.keyAsHex == controllerKey?.keyAsHex

    internal companion object {

        @JvmSynthetic
        internal fun createUnacknowledgedSession(
            sessionTopic: Topic,
            proposal: PairingParamsVO.SessionProposeParams,
            selfParticipant: SessionParticipantVO,
            sessionExpiry: Long,
            namespaces: Map<String, EngineDO.Namespace.Session>
        ): SessionVO {
            return SessionVO(
                sessionTopic,
                Expiry(sessionExpiry),
                relayProtocol = proposal.relays.first().protocol,
                relayData = proposal.relays.first().data,
                peerPublicKey = PublicKey(proposal.proposer.publicKey),
                peerMetaData = proposal.proposer.metadata,
                selfPublicKey = PublicKey(selfParticipant.publicKey),
                selfMetaData = selfParticipant.metadata,
                controllerKey = PublicKey(selfParticipant.publicKey),
                namespaces = namespaces.toMapOfNamespacesVOSession(),
                proposalNamespaces = proposal.namespaces,
                isAcknowledged = false
            )
        }

        @JvmSynthetic
        internal fun createAcknowledgedSession(
            sessionTopic: Topic,
            settleParams: SessionParamsVO.SessionSettleParams,
            selfPublicKey: PublicKey,
            selfMetadata: MetaData,
            proposalNamespaces: Map<String, NamespaceVO.Proposal>
        ): SessionVO {
            return SessionVO(
                sessionTopic,
                Expiry(settleParams.expiry),
                relayProtocol = settleParams.relay.protocol,
                relayData = settleParams.relay.data,
                peerPublicKey = PublicKey(settleParams.controller.publicKey),
                peerMetaData = settleParams.controller.metadata,
                selfPublicKey = selfPublicKey,
                selfMetaData = selfMetadata,
                controllerKey = PublicKey(settleParams.controller.publicKey),
                namespaces = settleParams.namespaces,
                proposalNamespaces = proposalNamespaces,
                isAcknowledged = true
            )
        }
    }
}