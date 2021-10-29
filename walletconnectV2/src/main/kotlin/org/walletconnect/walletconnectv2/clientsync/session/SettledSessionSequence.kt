package org.walletconnect.walletconnectv2.clientsync.session

import org.walletconnect.walletconnectv2.clientsync.session.proposal.RelayProtocolOptions
import org.walletconnect.walletconnectv2.common.Expiry
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.crypto.data.PublicKey
import org.walletconnect.walletconnectv2.clientsync.session.success.SessionState

data class SettledSessionSequence(
    val settledTopic: Topic,
    val relay: RelayProtocolOptions,
    val selfPublicKey: PublicKey,
    val peerPublicKey: PublicKey,
    val sharedKey: String,
    val expiry: Expiry,
    val state: SessionState
)