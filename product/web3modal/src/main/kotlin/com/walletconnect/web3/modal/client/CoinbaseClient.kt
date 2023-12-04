package com.walletconnect.web3.modal.client

import android.content.Context
import android.net.Uri
import com.coinbase.android.nativesdk.CoinbaseWalletSDK
import com.coinbase.android.nativesdk.message.request.Web3JsonRPC
import com.coinbase.android.nativesdk.message.response.ActionResult
import com.walletconnect.android.internal.common.model.AppMetaData

internal class CoinbaseClient(
    context: Context,
    appMetaData: AppMetaData,
) {

    private val coinbaseWalletSDK = CoinbaseWalletSDK(
        appContext = context,
        domain = Uri.parse(appMetaData.url),
        openIntent = { intent -> context.startActivity(intent) }
    )

    fun connect(
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val requestAccount = Web3JsonRPC.RequestAccounts().action()
        val handShakeActions = listOf(requestAccount)

        coinbaseWalletSDK.initiateHandshake(handShakeActions) { result, account ->
            result.onSuccess { actionResults: List<ActionResult> -> onSuccess() }
            result.onFailure { err -> onError(err) }
        }
    }

}