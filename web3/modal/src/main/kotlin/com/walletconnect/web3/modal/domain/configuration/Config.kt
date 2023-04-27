package com.walletconnect.web3.modal.domain.configuration

import com.squareup.moshi.*
import com.walletconnect.sign.client.Sign

internal const val CONFIGURATION = "modal_config"

@JsonClass(generateAdapter = true, generator = "sealed:config")
sealed class Config {

    @JsonClass(generateAdapter = true)
    data class Connect(
        val namespaces: Map<String, Sign.Model.Namespace.Proposal>? = null,
        val optionalNamespaces: Map<String, Sign.Model.Namespace.Proposal>? = null,
        @EncodedString
        val uri: String? = null
    ): Config()
}
