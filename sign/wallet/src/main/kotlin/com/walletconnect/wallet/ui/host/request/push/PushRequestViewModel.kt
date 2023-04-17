package com.walletconnect.wallet.ui.host.request.push

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.push.common.Push
import com.walletconnect.push.wallet.client.PushWalletClient
import com.walletconnect.sample_common.tag
import com.walletconnect.wallet.ui.SampleWalletEvents
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PushRequestViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<PushRequestUI> = MutableStateFlow(PushRequestUI.Initial)
    val uiState: StateFlow<PushRequestUI> = _uiState.asStateFlow()

    private val _event: MutableSharedFlow<SampleWalletEvents> = MutableSharedFlow()
    val event: SharedFlow<SampleWalletEvents> = _event.asSharedFlow()

    fun loadRequestData(arrayOfArgs: ArrayList<String?>) {
        val requestId: Long = arrayOfArgs[0].toString().toLong()
        val peerName: String? = arrayOfArgs[1]
        val peerDesc: String? = arrayOfArgs[2]
        val icon: String? = arrayOfArgs[3]

        _uiState.value = PushRequestUI.Content(requestId, peerName, peerDesc, icon)
    }

    fun reject() {
        (uiState.value as? PushRequestUI.Content)?.let { pushRequest ->
            val rejectParams = Push.Wallet.Params.Reject(pushRequest.requestId, "Kotlin Wallet Error")

            PushWalletClient.reject(
                rejectParams,
                onSuccess = { Log.i(tag(this), "Rejected Successfully") },
                onError = { error -> Log.e(tag(this), error.throwable.stackTraceToString()) }
            )

            viewModelScope.launch {
                _event.emit(SampleWalletEvents.PushRequestResponded)
            }
        }
    }

    fun approve() {
        (uiState.value as? PushRequestUI.Content)?.let { pushRequest ->
            val approveParams = Push.Wallet.Params.Approve(pushRequest.requestId) {
                Push.Model.Cacao.Signature("", "", "")
            }

            PushWalletClient.approve(
                approveParams,
                onSuccess = { Log.i(tag(this), "Approved Successfully") },
                onError = { error ->
                    Log.e(tag(this), error.throwable.stackTraceToString())
                }
            )

            viewModelScope.launch {
                _event.emit(SampleWalletEvents.PushRequestResponded)
            }
        }
    }
}