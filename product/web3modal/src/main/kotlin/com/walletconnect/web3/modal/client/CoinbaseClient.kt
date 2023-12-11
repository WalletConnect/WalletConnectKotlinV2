package com.walletconnect.web3.modal.client

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityOptionsCompat
import com.coinbase.android.nativesdk.CoinbaseWalletSDK
import com.coinbase.android.nativesdk.message.request.Web3JsonRPC
import com.coinbase.android.nativesdk.message.response.ActionResult
import com.walletconnect.android.internal.common.model.AppMetaData

internal class CoinbaseClient(
    context: Context,
    appMetaData: AppMetaData,
) {
    private val activityLauncher = ActivityResultLauncherHolder<Intent>()

    val coinbaseWalletSDK = CoinbaseWalletSDK(
        appContext = context,
        domain = Uri.parse(appMetaData.url),
        openIntent = { activityLauncher.launch(it) }
    )

    fun handleResponse(uri: Uri) {
        coinbaseWalletSDK.handleResponse(uri)
    }

    fun setLauncher(launcher: ActivityResultLauncher<Intent>) {
        activityLauncher.launcher = launcher
    }

    fun connect(
        onSuccess: (String) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val requestAccount = Web3JsonRPC.RequestAccounts().action()
        val handShakeActions = listOf(requestAccount)

        coinbaseWalletSDK.initiateHandshake(handShakeActions) { result, account ->
            result.onSuccess { actionResults: List<ActionResult> ->
//                {"chain":"eth","networkId":1,"address":"0x8ea13985153989d9ebDB94dC45F46398c4f6858c"}

//                Modal.Model.ApprovedSession(
//                    topic = "",
//                    metaData = null,
//                    namespaces = li
//                )
            }
            result.onFailure { err -> onError(err) }
        }
    }

    fun request(
        request: String,
        onSuccess: (String) -> Unit,
        onError: (Throwable) -> Unit
    ) {


    }

    internal class ActivityResultLauncherHolder<I> {
        var launcher: ActivityResultLauncher<I>? = null

        fun launch(input: I?, options: ActivityOptionsCompat? = null) {
            launcher?.launch(input, options) ?: error("Launcher has not been initialized")
        }

        fun unregister() {
            launcher?.unregister() ?: error("Launcher has not been initialized")
        }
    }
}
