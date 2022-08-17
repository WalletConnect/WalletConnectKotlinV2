package com.walletconnect.auth.engine.model

internal sealed class EngineDO {

    internal data class AppMetaData(
        val name: String,
        val description: String,
        val url: String,
        val icons: List<String>,
        val redirect: String?,
    ) : EngineDO()
}