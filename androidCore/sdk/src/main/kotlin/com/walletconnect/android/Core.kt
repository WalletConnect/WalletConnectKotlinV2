package com.walletconnect.android

object Core {
    sealed interface Listeners {
        interface SessionPing : Listeners {
            fun onSuccess(pingSuccess: Model.Ping.Success)
            fun onError(pingError: Model.Ping.Error)
        }
    }

    sealed class Model {
        data class Error(val throwable: Throwable) : Model()

        sealed class Ping : Model() {
            data class Success(val topic: String) : Ping()
            data class Error(val error: Throwable) : Ping()
        }

        data class AppMetaData(val name: String, val description: String, val url: String, val icons: List<String>, val redirect: String?) : Model()

        data class SessionProposal(
            val name: String,
        /*    val description: String,
            val url: String,
            val icons: List<URI>,
            val requiredNamespaces: Map<String, LocalSocketAddress.Namespace.Proposal>,
            val proposerPublicKey: String,
            val relayProtocol: String,
            val relayData: String?,*/
        ) : Model()

        sealed class DeletedSession : Model() {

            data class Success(val topic: String, val reason: String) : DeletedSession()

            data class Error(val error: Throwable) : DeletedSession()
        }
    }

    sealed class Params {

        data class Ping(val topic: String) : Params()

        data class Pair(val uri: String) : Params()
    }
}