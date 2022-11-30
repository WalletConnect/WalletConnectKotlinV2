package com.walletconnect.android

import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.Expiry


object Core {
    sealed interface Listeners {
        interface PairingPing : Listeners {
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

        data class DeletedPairing(val topic: String, val reason: String) : Model()

        data class Pairing(
            val topic: String,
            val expiry: Long,
            val peerAppMetaData: AppMetaData? = null,
            val relayProtocol: String,
            val relayData: String?,
            val uri: String,
            val isActive: Boolean,
            val registeredMethods: String
        ) : Model()
    }

    sealed class Params {

        data class Ping(val topic: String) : Params()

        data class Pair(val uri: String) : Params()

        data class Disconnect(val topic: String) : Params()

        data class Activate(val topic: String) : Params()

        data class UpdateExpiry(val topic: String, val expiry: Expiry) : Params()

        data class UpdateMetadata(val topic: String, val metadata: Model.AppMetaData, val metaDataType: AppMetaDataType) : Params()
    }
}