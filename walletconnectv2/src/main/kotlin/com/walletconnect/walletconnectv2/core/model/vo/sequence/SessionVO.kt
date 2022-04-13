package com.walletconnect.walletconnectv2.core.model.vo.sequence

import com.walletconnect.walletconnectv2.core.model.type.Sequence
import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.MetaDataVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.SessionParticipantVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.params.PairingParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.params.SessionParamsVO

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
    val methods: List<String>,
    val events: List<String>,
    val isAcknowledged: Boolean,
) : Sequence {
    val isPeerController: Boolean = peerPublicKey?.keyAsHex == controllerKey?.keyAsHex
    val isSelfController: Boolean = selfPublicKey.keyAsHex == controllerKey?.keyAsHex
    val chains: List<String> get() = getChainIds(accounts)

    internal companion object {

        @JvmSynthetic
        internal fun createUnacknowledgedSession(
            sessionTopic: TopicVO,
            proposal: PairingParamsVO.SessionProposeParams,
            selfParticipant: SessionParticipantVO,
            sessionExpiry: Long,
            accounts: List<String>,
            methods: List<String>,
            events: List<String>,
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
                methods = methods,
                events = events,
                accounts = accounts,
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
                methods = settleParams.methods,
                events = settleParams.events,
                accounts = settleParams.accounts,
                isAcknowledged = true
            )
        }

        fun getChainIds(accountIds: List<String>): List<String> {
            val chains = mutableListOf<String>()
            accountIds.forEach { accountId ->
                chains.add(getChainId(accountId))
            }
            return chains
        }

        private fun getChainId(accountId: String): String {
            val elements = accountId.split(":")
            val (namespace, reference) = Pair(elements[0], elements[1])
            return "$namespace:$reference"
        }
    }
}