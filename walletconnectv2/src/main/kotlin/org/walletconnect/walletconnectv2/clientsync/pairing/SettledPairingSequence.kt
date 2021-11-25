package org.walletconnect.walletconnectv2.clientsync.pairing

import org.json.JSONObject
import org.walletconnect.walletconnectv2.clientsync.pairing.before.proposal.PairingPermissions
import org.walletconnect.walletconnectv2.clientsync.pairing.before.proposal.PairingProposedPermissions
import org.walletconnect.walletconnectv2.common.Expiry
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.crypto.data.PublicKey

data class SettledPairingSequence(
    val settledTopic: Topic,
    val relay: JSONObject,
    val selfPublicKey: PublicKey,
    val peerPublicKey: PublicKey,
    val sequencePermissions: PairingPermissions,
    val expiry: Expiry
)