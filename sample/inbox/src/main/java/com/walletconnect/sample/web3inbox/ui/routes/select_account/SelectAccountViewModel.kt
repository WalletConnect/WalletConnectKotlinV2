package com.walletconnect.sample.web3inbox.ui.routes.select_account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.sample.web3inbox.domain.WCMDelegate
import com.walletconnect.sample.web3inbox.ui.routes.W3ISampleEvents
import com.walletconnect.wcmodal.client.Modal
import com.walletconnect.wcmodal.client.WalletConnectModal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SelectAccountViewModel() : ViewModel() {

    val walletEvents = WCMDelegate.wcEventModels.map { walletEvent: Modal.Model? ->
        when (walletEvent) {
            is Modal.Model.ApprovedSession -> W3ISampleEvents.SessionApproved(walletEvent.accounts.first())
            is Modal.Model.RejectedSession -> W3ISampleEvents.SessionRejected
            else -> W3ISampleEvents.NoAction
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    fun disconnectOldSessions() {
        WalletConnectModal.getListOfActiveSessions().forEach {
            WalletConnectModal.disconnect(Modal.Params.Disconnect(it.topic), onSuccess = {}, onError = { error -> Timber.e(error.throwable) })
        }
    }

    fun connectToWallet(pairingTopicPosition: Int = -1, onProposedSequence: (String) -> Unit = {}) {
        val pairing: Core.Model.Pairing = if (pairingTopicPosition > -1) {
            CoreClient.Pairing.getPairings()[pairingTopicPosition]
        } else {
            CoreClient.Pairing.create() { error ->
                throw IllegalStateException("Creating Pairing failed: ${error.throwable.stackTraceToString()}")
            }!!
        }

        val namespaces: Map<String, Modal.Model.Namespace.Proposal> = mapOf(
            "eip155" to Modal.Model.Namespace.Proposal(
                chains = listOf("eip155:1"),
                methods = listOf(
                    "eth_sendTransaction",
                    "personal_sign",
                    "eth_sign",
                    "eth_signTypedData"
                ),
                events = emptyList()
            )
        )


        //note: this property is not used in the SDK, only for demonstration purposes
        val expiry = (System.currentTimeMillis() / 1000) + TimeUnit.SECONDS.convert(7, TimeUnit.DAYS)
        val properties: Map<String, String> = mapOf("sessionExpiry" to "$expiry")

        val connectParams =
            Modal.Params.Connect(
                namespaces = namespaces.toMutableMap(),
                optionalNamespaces = null,
                properties = properties,
                pairing = pairing
            )

        WalletConnectModal.connect(connectParams,
            onSuccess = {
                viewModelScope.launch(Dispatchers.Main) {
                    onProposedSequence(pairing.uri)
                }
            },
            onError = { error ->
                Timber.e(error.throwable)
            }
        )

    }
}