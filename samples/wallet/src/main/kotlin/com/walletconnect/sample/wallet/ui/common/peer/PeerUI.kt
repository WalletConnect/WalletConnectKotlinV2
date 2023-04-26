package com.walletconnect.sample.wallet.ui.common.peer

import androidx.compose.ui.graphics.Color
import com.walletconnect.web3.wallet.client.Wallet


data class PeerUI(
    val peerIcon: String,
    val peerName: String,
    val peerUri: String,
    val peerDescription: String,
) {
    companion object {
        val Empty = PeerUI("", "", "", "")
    }
}

data class PeerContextUI(
    val origin: String,
    val validation: Validation,
    val verifyUrl: String
)

enum class Validation {
    VERIFIED, UNVERIFIED, UNKNOWN
}

val Orange = Color(0xFFFF9F33)
val Red = Color(0xFFC70039)
val Green = Color(0xFF60E353)

fun Wallet.Model.SessionContext.toPeerUI(): PeerContextUI =
    PeerContextUI(origin, validation.toUI(), verifyUrl)

fun Wallet.Model.AuthContext.toPeerUI(): PeerContextUI =
    PeerContextUI(origin, validation.toUI(), verifyUrl)

fun Wallet.Model.Validation.toUI(): Validation =
    when (this) {
        Wallet.Model.Validation.VALID -> Validation.VERIFIED
        Wallet.Model.Validation.INVALID -> Validation.UNVERIFIED
        Wallet.Model.Validation.UNKNOWN -> Validation.UNKNOWN
    }