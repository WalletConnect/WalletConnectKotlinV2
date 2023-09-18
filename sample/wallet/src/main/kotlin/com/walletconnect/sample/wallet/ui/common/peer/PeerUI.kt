package com.walletconnect.sample.wallet.ui.common.peer

import androidx.compose.ui.graphics.Color
import com.walletconnect.sample.common.ui.theme.mismatch_color
import com.walletconnect.sample.common.ui.theme.unverified_color
import com.walletconnect.sample.common.ui.theme.verified_color
import com.walletconnect.sample.wallet.R
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

fun Wallet.Model.VerifyContext.toPeerUI(): PeerContextUI =
    PeerContextUI(origin, validation.toUI(), verifyUrl, isScam)

fun Wallet.Model.Validation.toUI(): Validation =
    when (this) {
        Wallet.Model.Validation.VALID -> Validation.VALID
        Wallet.Model.Validation.INVALID -> Validation.INVALID
        Wallet.Model.Validation.UNKNOWN -> Validation.UNKNOWN
    }

fun getValidationColor(validation: Validation): Color {
    return when (validation) {
        Validation.VALID -> verified_color
        Validation.UNKNOWN -> unverified_color
        Validation.INVALID -> mismatch_color
    }
}

fun getValidationIcon(validation: Validation): Int {
    return when (validation) {
        Validation.VALID -> R.drawable.ic_verified
        Validation.UNKNOWN -> R.drawable.ic_cannot_verify
        Validation.INVALID -> R.drawable.invalid_domain
    }
}

fun getValidationTitle(validation: Validation): String {
    return when (validation) {
        Validation.VALID -> "Verified domain"
        Validation.UNKNOWN -> "Cannot verify"
        Validation.INVALID -> "Invalid domain"
    }
}