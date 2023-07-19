package com.walletconnect.modals.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.CoreClient
import com.walletconnect.modals.ModalSampleDelegate
import com.walletconnect.sample.common.Chains
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

class HomeViewModel : ViewModel() {

    val walletEvents = ModalSampleDelegate.wcEventModels.map { walletEvent: Modal.Model? ->
        Timber.d(walletEvent.toString())
    }.shareIn(viewModelScope, SharingStarted.Eagerly)

    fun connectYourWallet(
        onSuccess: (String) -> Unit
    ) {
        val pairing = CoreClient.Pairing.create { error -> throw IllegalStateException("Creating Pairing failed: ${error.throwable}") }!!
        val ethereumChain = Chains.ETHEREUM_MAIN
        val namespace = mapOf(
            ethereumChain.chainId to Modal.Model.Namespace.Proposal(
                chains = listOf(ethereumChain.chainId),
                methods = ethereumChain.methods,
                events = ethereumChain.events
            )
        )

        //note: this property is not used in the SDK, only for demonstration purposes
        val expiry = (System.currentTimeMillis() / 1000) + TimeUnit.SECONDS.convert(7, TimeUnit.DAYS)
        val properties: Map<String, String> = mapOf("sessionExpiry" to "$expiry")

        val connectParams = Modal.Params.Connect(
            namespaces = namespace,
            optionalNamespaces = null,
            properties = properties,
            pairing = pairing
        )

        Web3Modal.connect(connectParams,
            onSuccess = {
                viewModelScope.launch {
                    onSuccess(pairing.uri)
                }
            },
            onError = { error -> Timber.e(error.throwable) })
    }
}
