package com.walletconnect.android.echo

sealed class Message {
    data class Notify(
        val title: String,
        val body: String,
        val icon: String?,
        val url: String?,
        val type: String
    ) : Message()

    data class Simple(
        val title: String,
        val body: String
    ) : Message()

    data class Decrypted(
        val metadata: Metadata,
        val request: Request
    ) : Message() {
        data class Metadata(
            val name: String,
            val description: String,
            val url: String,
            val icons: List<String>,
        )

        data class Request(
            val id: Long,
            val method: String,
            val params: String,
        )
    }
}