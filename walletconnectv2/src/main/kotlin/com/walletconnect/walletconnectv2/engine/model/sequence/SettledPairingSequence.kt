package com.walletconnect.walletconnectv2.engine.model.sequence

import org.json.JSONObject
import com.walletconnect.walletconnectv2.relay.model.clientsync.pairing.before.proposal.PairingPermissions
import com.walletconnect.walletconnectv2.common.model.Expiry
import com.walletconnect.walletconnectv2.common.model.Topic
import com.walletconnect.walletconnectv2.crypto.model.PublicKey

internal data class SettledPairingSequence(
    val settledTopic: Topic,
    val relay: JSONObject,
    val selfPublicKey: PublicKey,
    val peerPublicKey: PublicKey,
    val sequencePermissions: PairingPermissions,
    val expiry: Expiry
)