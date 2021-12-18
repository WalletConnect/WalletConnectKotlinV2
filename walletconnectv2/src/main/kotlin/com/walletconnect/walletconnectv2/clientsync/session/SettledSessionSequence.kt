package com.walletconnect.walletconnectv2.clientsync.session

import com.walletconnect.walletconnectv2.clientsync.session.before.proposal.RelayProtocolOptions
import com.walletconnect.walletconnectv2.clientsync.session.common.SessionState
import com.walletconnect.walletconnectv2.common.Expiry
import com.walletconnect.walletconnectv2.common.Topic
import com.walletconnect.walletconnectv2.crypto.data.PublicKey
import com.walletconnect.walletconnectv2.crypto.data.SharedKey

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