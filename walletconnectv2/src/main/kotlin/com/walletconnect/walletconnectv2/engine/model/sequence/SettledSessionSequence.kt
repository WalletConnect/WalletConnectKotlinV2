package com.walletconnect.walletconnectv2.engine.model.sequence

import com.walletconnect.walletconnectv2.relay.model.clientsync.session.before.proposal.RelayProtocolOptions
import com.walletconnect.walletconnectv2.relay.model.clientsync.session.common.SessionState
import com.walletconnect.walletconnectv2.common.model.Expiry
import com.walletconnect.walletconnectv2.common.model.Topic
import com.walletconnect.walletconnectv2.crypto.model.PublicKey
import com.walletconnect.walletconnectv2.crypto.model.SharedKey

data class SettledSessionSequence(
    val topic: Topic,
    val relay: RelayProtocolOptions,
    val selfPublicKey: PublicKey,
    val peerPublicKey: PublicKey,
    val permissions: SettledSessionPermissions,
    val sharedKey: SharedKey,
    val expiry: Expiry,
    val state: SessionState
)

data class SettledSessionPermissions(
    val controller: Controller
)

data class Controller(
    val publicKey: String
)