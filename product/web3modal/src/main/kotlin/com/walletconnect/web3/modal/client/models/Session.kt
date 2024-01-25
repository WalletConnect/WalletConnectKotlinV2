package com.walletconnect.web3.modal.client.models

import com.walletconnect.android.Core
import com.walletconnect.web3.modal.client.Modal

sealed class Session {

    data class WalletConnectSession(
        val pairingTopic: String,
        val topic: String,
        val expiry: Long,
        val namespaces: Map<String, Modal.Model.Namespace.Session>,
        val metaData: Core.Model.AppMetaData?,
    ) : Session() {
        val redirect: String? = metaData?.redirect
    }

    data class CoinbaseSession(
        val chain: String,
        val address: String
    ) : Session()
}
