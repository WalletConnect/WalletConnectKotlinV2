package org.walletconnect.walletconnectv2.engine.model

import java.net.URI

sealed class EngineData {

    data class SessionProposal(
        val name: String,
        val description: String,
        val url: String,
        val icons: List<URI>,
        val chains: List<String>,
        var methods: List<String>,
        val topic: String,
        val proposerPublicKey: String,
        val ttl: Long
    ) : EngineData() {
        val icon: String = icons.first().toString()
    }

    data class SessionRequest(
        val topic: String,
        val request: Any,
        val chainId: String?,
        val method: String
    ) : EngineData()

    data class SettledSession(
        var icon: String?,
        var name: String,
        var uri: String,
        val topic: String
    ) : EngineData()
}