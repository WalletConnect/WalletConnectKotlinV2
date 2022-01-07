package com.walletconnect.walletconnectv2.relay.model.clientsync.pairing.before.proposal

import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.relay.model.clientsync.pairing.before.success.PairingParticipant

@JsonClass(generateAdapter = true)
internal data class PairingPermissions(
    val controller: PairingParticipant
)
