package com.walletconnect.sample.dapp.web3modal.ui

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.sample.dapp.domain.DappDelegate
import com.walletconnect.sign.client.Sign
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import timber.log.Timber

class Web3ModalViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val uri: String = Uri.decode(savedStateHandle.get<String>(uriArgId) ?: "")

    val walletEvents = DappDelegate.wcEventModels.map { walletEvent: Sign.Model? ->
        when (walletEvent) {
            is Sign.Model.ApprovedSession -> {
                //Subscribe to pushes
                Timber.d("ApprovedSession")
                Web3ModalEvents.SessionApproved
            }
            is Sign.Model.RejectedSession -> {
                Timber.d("RejectedSession")
                Web3ModalEvents.SessionRejected
            }
            else -> Web3ModalEvents.NoAction
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())
}