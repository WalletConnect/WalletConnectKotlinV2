package com.walletconnect.sample.wallet.ui.common

data class ImageUrl(
    val small: String,
    val medium: String,
    val large: String,
)

fun List<String>.toImageUrl(): ImageUrl {
    return ImageUrl(
        small = this.getOrElse(0) { "" },
        medium = this.getOrElse(1) { "" },
        large = this.getOrElse(2) { "" },
    )
}