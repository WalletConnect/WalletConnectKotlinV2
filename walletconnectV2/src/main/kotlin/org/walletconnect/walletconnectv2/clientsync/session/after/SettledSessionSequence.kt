package org.walletconnect.walletconnectv2.clientsync.session.after

import org.walletconnect.walletconnectv2.clientsync.session.before.proposal.RelayProtocolOptions
import org.walletconnect.walletconnectv2.clientsync.session.before.success.SessionState
import org.walletconnect.walletconnectv2.common.Expiry
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.crypto.data.PublicKey

data class SettledSessionSequence(
    val settledTopic: Topic,
    val relay: RelayProtocolOptions,
    val selfPublicKey: PublicKey,
    val peerPublicKey: PublicKey,
    val sharedKey: String,
    val expiry: Expiry,
    val state: SessionState
)