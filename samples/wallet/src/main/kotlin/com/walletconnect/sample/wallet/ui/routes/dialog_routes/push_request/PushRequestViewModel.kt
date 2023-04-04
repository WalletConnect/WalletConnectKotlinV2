package com.walletconnect.sample.wallet.ui.routes.dialog_routes.push_request

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.walletconnect.android.cacao.sign
import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.push.common.Push
import com.walletconnect.push.common.cacao.CacaoSigner
import com.walletconnect.push.wallet.client.PushWalletClient
import com.walletconnect.sample.wallet.domain.EthAccountDelegate
import com.walletconnect.sample.wallet.domain.hexToBytes
import com.walletconnect.sample.wallet.ui.common.peer.PeerUI
import com.walletconnect.sample_common.tag
import java.net.URLDecoder

class PushRequestViewModel : ViewModel() {

    fun reject(requestId: Long, navigateBack: () -> Unit) {
        val rejectParams = Push.Wallet.Params.Reject(requestId, "Kotlin Wallet Rejected Push Request")

        PushWalletClient.reject(
            rejectParams,
            onSuccess = {
                Log.i(tag(this), "Rejected Successfully")
                navigateBack()
            },
            onError = { error ->
                Log.e(tag(this), error.throwable.stackTraceToString())
                Firebase.crashlytics.recordException(error.throwable)
                navigateBack()
            }
        )
    }

    fun approve(requestId: Long, navigateBack: () -> Unit) {

        val approveParams = Push.Wallet.Params.Approve(requestId) { message ->
            CacaoSigner.sign(message, EthAccountDelegate.privateKey.hexToBytes(), SignatureType.EIP191)
        }

        PushWalletClient.approve(
            approveParams,
            onSuccess = {
                Log.i(tag(this), "Approved Successfully")
                navigateBack()
            },
            onError = { error ->
                Log.e(tag(this), error.throwable.stackTraceToString())
                Firebase.crashlytics.recordException(error.throwable)
                navigateBack()
            }
        )
    }

    fun generatePushRequestUI(requestId: Long, encodedPeerName: String, encodedPeerDesc: String, encodedIconUrl: String?, encodedRedirect: String?): PushRequestUI {
        val decodedPeerName = URLDecoder.decode(encodedPeerName, Charsets.UTF_8.name())
        val decodedPeerDesc = URLDecoder.decode(encodedPeerDesc, Charsets.UTF_8.name())
        val decodedIconUrl = encodedIconUrl?.run { URLDecoder.decode(this, Charsets.UTF_8.name()) }
        val decodedRedirect = encodedRedirect?.run { URLDecoder.decode(this, Charsets.UTF_8.name()) }

        return PushRequestUI(
            requestId = requestId,
            peerUI = PeerUI(
                peerName = decodedPeerName,
                peerIcon = decodedIconUrl ?: "",
                peerUri = decodedRedirect ?: "",
                peerDescription = decodedPeerDesc,
            )
        )
    }
}