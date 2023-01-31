@file:JvmSynthetic

package com.walletconnect.sign.common.model.vo.sequence

import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.type.Sequence
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.common.model.vo.clientsync.common.SessionParticipantVO
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toMapOfNamespacesVOSession

internal data class SessionVO(
    override val topic: Topic,
    override val expiry: Expiry,
    val relayProtocol: String,
    val relayData: String?,
    val controllerKey: PublicKey? = null,
    val selfPublicKey: PublicKey,
    val selfAppMetaData: AppMetaData? = null,
    val peerPublicKey: PublicKey? = null,
    val peerAppMetaData: AppMetaData? = null,
    val sessionNamespaces: Map<String, NamespaceVO.Session>,
    val requiredNamespaces: Map<String, NamespaceVO.Required>,
    val optionalNamespaces: Map<String, NamespaceVO.Optional>?,
    val isAcknowledged: Boolean,
) : Sequence {
    val isPeerController: Boolean = peerPublicKey?.keyAsHex == controllerKey?.keyAsHex
    val isSelfController: Boolean = selfPublicKey.keyAsHex == controllerKey?.keyAsHex

    internal companion object {

        @JvmSynthetic
        internal fun createUnacknowledgedSession(
            sessionTopic: Topic,
            proposal: SignParams.SessionProposeParams,
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
                peerAppMetaData = proposal.proposer.metadata,
                selfPublicKey = PublicKey(selfParticipant.publicKey),
                selfAppMetaData = selfParticipant.metadata,
                controllerKey = PublicKey(selfParticipant.publicKey),
                sessionNamespaces = namespaces.toMapOfNamespacesVOSession(),
                requiredNamespaces = proposal.requiredNamespaces,
                optionalNamespaces = proposal.optionalNamespaces,
                isAcknowledged = false
            )
        }

        @JvmSynthetic
        internal fun createAcknowledgedSession(
            sessionTopic: Topic,
            settleParams: SignParams.SessionSettleParams,
            selfPublicKey: PublicKey,
            selfMetadata: AppMetaData,
            proposalNamespaces: Map<String, NamespaceVO.Required>,
            optionalNamespaces: Map<String, NamespaceVO.Optional>?
        ): SessionVO {
            return SessionVO(
                sessionTopic,
                Expiry(settleParams.expiry),
                relayProtocol = settleParams.relay.protocol,
                relayData = settleParams.relay.data,
                peerPublicKey = PublicKey(settleParams.controller.publicKey),
                peerAppMetaData = settleParams.controller.metadata,
                selfPublicKey = selfPublicKey,
                selfAppMetaData = selfMetadata,
                controllerKey = PublicKey(settleParams.controller.publicKey),
                sessionNamespaces = settleParams.namespaces,
                requiredNamespaces = proposalNamespaces,
                optionalNamespaces = optionalNamespaces,
                isAcknowledged = true
            )
        }
    }
}