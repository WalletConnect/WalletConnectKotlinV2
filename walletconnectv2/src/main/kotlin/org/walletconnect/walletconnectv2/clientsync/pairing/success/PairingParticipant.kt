package org.walletconnect.walletconnectv2.clientsync.pairing.success

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PairingParticipant(val publicKey: String)