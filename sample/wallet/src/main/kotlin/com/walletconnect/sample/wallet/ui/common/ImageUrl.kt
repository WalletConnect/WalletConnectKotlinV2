package com.walletconnect.sample.wallet.ui.common

data class ImageUrl(
    val sm: String,
    val md: String,
    val lg: String,
)

fun List<String>.toImageUrl(): ImageUrl {
    return ImageUrl(
        sm = this.getOrElse(0) { "" },
        md = this.getOrElse(0) { "" },
        lg = this.getOrElse(0) { "" },
    )
}