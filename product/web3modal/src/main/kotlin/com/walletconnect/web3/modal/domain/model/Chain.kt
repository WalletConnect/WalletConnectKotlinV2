package com.walletconnect.web3.modal.domain.model

import com.walletconnect.web3.modal.utils.networkImagesIds
import com.walletconnect.web3.modal.utils.networkNames

internal data class Chain(
    val id: String,
) {
    val namespace: String
    val reference: String
    val imageUrl: String
    val name: String

    init {
        val chainData = id.split(":")
        namespace = chainData.first()
        reference = chainData.last()
        name = networkNames[reference] ?: id
        imageUrl = "https://api.web3modal.com/public/getAssetImage/${networkImagesIds[reference]}"
    }
}
