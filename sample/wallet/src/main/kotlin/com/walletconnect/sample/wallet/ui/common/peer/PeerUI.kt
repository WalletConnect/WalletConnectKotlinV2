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
    val verifyUrl: String,
    val isScam: Boolean?
)

enum class Validation {
    VALID, INVALID, UNKNOWN
}

val Orange = Color(0xFFFF9F33)
val Red = Color(0xFFC70039)
val Green = Color(0xFF60E353)

fun Wallet.Model.VerifyContext.toPeerUI(): PeerContextUI =
    PeerContextUI(origin, validation.toUI(), verifyUrl, isScam)

fun Wallet.Model.Validation.toUI(): Validation =
    when (this) {
        Wallet.Model.Validation.VALID -> Validation.VALID
        Wallet.Model.Validation.INVALID -> Validation.INVALID
        Wallet.Model.Validation.UNKNOWN -> Validation.UNKNOWN
    }