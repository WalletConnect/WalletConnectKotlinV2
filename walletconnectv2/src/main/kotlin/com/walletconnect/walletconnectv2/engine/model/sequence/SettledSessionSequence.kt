package com.walletconnect.walletconnectv2.engine.model.sequence

import com.walletconnect.walletconnectv2.relay.model.clientsync.session.before.proposal.RelayProtocolOptions
import com.walletconnect.walletconnectv2.relay.model.clientsync.session.common.SessionState
import com.walletconnect.walletconnectv2.common.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.common.model.vo.TopicVO
import com.walletconnect.walletconnectv2.crypto.model.PublicKey
import com.walletconnect.walletconnectv2.crypto.model.SharedKey

internal data class SettledSessionSequence(
    val topic: TopicVO,
    val relay: RelayProtocolOptions,
    val selfPublicKey: PublicKey,
    val peerPublicKey: PublicKey,
    val permissions: SettledSessionPermissions,
    val sharedKey: SharedKey,
    val expiry: ExpiryVO,
    val state: SessionState
)

internal data class SettledSessionPermissions(
    val controller: Controller
)

internal data class Controller(
    val publicKey: String
)