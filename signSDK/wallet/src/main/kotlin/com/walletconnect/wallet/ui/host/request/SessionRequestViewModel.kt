package com.walletconnect.wallet.ui.host.request

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.sample_common.tag
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.wallet.domain.WalletDelegate
import com.walletconnect.wallet.ui.SampleWalletEvents
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SessionRequestViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<SessionRequestUI> =
        MutableStateFlow(SessionRequestUI.Initial)
    val uiState: StateFlow<SessionRequestUI> = _uiState.asStateFlow()

    private val _event: MutableSharedFlow<SampleWalletEvents> = MutableSharedFlow()
    val event: SharedFlow<SampleWalletEvents> = _event.asSharedFlow()

    fun loadRequestData(arrayOfArgs: ArrayList<String?>) {
        val topic: String = arrayOfArgs[0].toString()
        val icon: String? = arrayOfArgs[1]
        val peerName: String? = arrayOfArgs[2]
        val requestId: Long = arrayOfArgs[3].toString().toLong()
        val param: String = arrayOfArgs[4].toString()
        val chain: String? = arrayOfArgs[5]
        val method: String = arrayOfArgs[6].toString()

        _uiState.value =
            SessionRequestUI.Content(topic, icon, peerName, requestId, param, chain, method)
    }

    fun reject(sendSessionRequestResponseDeepLink: (Uri) -> Unit) {
        (uiState.value as? SessionRequestUI.Content)?.let { sessionRequest ->
            val result = Sign.Params.Response(
                sessionTopic = sessionRequest.topic,
                jsonRpcResponse = Sign.Model.JsonRpcResponse.JsonRpcError(
                    id = sessionRequest.requestId,
                    code = 500,
                    message = "Kotlin Wallet Error"
                )
            )

            SignClient.respond(result) { error ->
                Log.e(tag(this), error.throwable.stackTraceToString())
            }

            viewModelScope.launch {
                _event.emit(SampleWalletEvents.SessionRequestResponded)
                WalletDelegate.clearCache()
            }

            sendResponseDeepLink(sessionRequest, sendSessionRequestResponseDeepLink)
        }
    }

    fun approve(sendSessionRequestResponseDeepLink: (Uri) -> Unit) {
        (uiState.value as? SessionRequestUI.Content)?.let { sessionRequest ->
            val result = Sign.Params.Response(
                sessionTopic = sessionRequest.topic,
                jsonRpcResponse = Sign.Model.JsonRpcResponse.JsonRpcResult(
                    sessionRequest.requestId,
                    "0xa3f20717a250c2b0b729b7e5becbff67fdaef7e0699da4de7ca5895b02a170a12d887fd3b17bfdce3481f10bea41f45ba9f709d39ce8325427b57afcfc994cee1b"
                )
            )

            SignClient.respond(result) { error ->
                Log.e(tag(this), error.throwable.stackTraceToString())
            }

            viewModelScope.launch {
                _event.emit(SampleWalletEvents.SessionRequestResponded)
                WalletDelegate.clearCache()
            }

            sendResponseDeepLink(sessionRequest, sendSessionRequestResponseDeepLink)
        }
    }

    private fun sendResponseDeepLink(
        sessionRequest: SessionRequestUI.Content,
        sendSessionRequestResponseDeepLink: (Uri) -> Unit,
    ) {
        val session = SignClient.getSettledSessionByTopic(sessionRequest.topic)
        val deepLinUri =
            session?.redirect?.toUri() ?: "wc:/${sessionRequest.topic})}/request".toUri()

        sendSessionRequestResponseDeepLink(deepLinUri)
    }
}