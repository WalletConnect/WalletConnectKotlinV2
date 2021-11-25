package org.walletconnect.walletconnectv2.clientsync.pairing.before.proposal

import com.squareup.moshi.JsonClass
import org.walletconnect.walletconnectv2.clientsync.pairing.before.success.PairingParticipant

@JsonClass(generateAdapter = true)
data class PairingPermissions(
    val controller: PairingParticipant
)
