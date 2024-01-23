package com.walletconnect.web3.modal.engine.coinbase

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityOptionsCompat
import com.coinbase.android.nativesdk.CoinbaseWalletSDK
import com.coinbase.android.nativesdk.message.request.ETH_REQUEST_ACCOUNTS
import com.coinbase.android.nativesdk.message.request.ETH_SEND_TRANSACTION
import com.coinbase.android.nativesdk.message.request.ETH_SIGN_TRANSACTION
import com.coinbase.android.nativesdk.message.request.ETH_SIGN_TYPED_DATA_V3
import com.coinbase.android.nativesdk.message.request.ETH_SIGN_TYPED_DATA_V4
import com.coinbase.android.nativesdk.message.request.PERSONAL_SIGN
import com.coinbase.android.nativesdk.message.request.RequestContent
import com.coinbase.android.nativesdk.message.request.WALLET_ADD_ETHEREUM_CHAIN
import com.coinbase.android.nativesdk.message.request.WALLET_SWITCH_ETHEREUM_CHAIN
import com.coinbase.android.nativesdk.message.request.WALLET_WATCH_ASSET
import com.coinbase.android.nativesdk.message.request.Web3JsonRPC
import com.coinbase.android.nativesdk.message.response.ActionResult
import com.coinbase.android.nativesdk.message.response.ResponseResult
import com.walletconnect.android.internal.common.modal.data.model.Wallet
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.util.Empty
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.models.request.Request
import com.walletconnect.web3.modal.domain.usecase.GetSelectedChainUseCase

internal const val COINBASE_WALLET_ID = "fd20dc426fb37566d803205b19bbc1d4096b248ac04548e3cfb6b3a38bd033aa"

internal fun Wallet.isCoinbaseWallet() = id == COINBASE_WALLET_ID

internal class CoinbaseClient(
    context: Context,
    appMetaData: AppMetaData,
) {
    private val activityLauncher = ActivityResultLauncherHolder<Intent>()
    private val getSelectedChainUseCase: GetSelectedChainUseCase by lazy { wcKoinApp.koin.get() }

    private val coinbaseWalletSDK = CoinbaseWalletSDK(
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

    fun unregister() {
        activityLauncher.unregister()
    }

    fun connect(
        onSuccess: (Modal.Model.ApprovedSession.CoinbaseSession) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val requestAccount = Web3JsonRPC.RequestAccounts().action()
        val handShakeActions = listOf(requestAccount)

        coinbaseWalletSDK.initiateHandshake(handShakeActions) { result, account ->
            result.onSuccess { _: List<ActionResult> ->
                account?.let {
                    val session = Modal.Model.ApprovedSession.CoinbaseSession(
                        chain = account.chain,
                        networkId = account.networkId.toString(),
                        address = account.address
                    )
                    onSuccess(session)
                }
            }
            result.onFailure { err -> onError(err) }
        }
    }

    fun request(
        request: Request,
        onSuccess: (results: List<CoinbaseResult>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        try {
            val chainId = getSelectedChainUseCase()
            val web3jsonRPC = request.mapToJson3JRPCRequest(chainId ?: String.Empty).action()
            coinbaseWalletSDK.makeRequest(
                RequestContent.Request(listOf(web3jsonRPC))
            ) { result: ResponseResult ->
                result.onSuccess {
                    onSuccess(it.toCoinbaseResults())
                }
                result.onFailure { onError(it) }
            }
        } catch (e: Throwable) {
            onError(e)
        }
    }

    fun disconnect() {
        coinbaseWalletSDK.resetSession()
    }

    @Throws
    private fun Request.mapToJson3JRPCRequest(chainId: String): Web3JsonRPC = when (method) {
        ETH_REQUEST_ACCOUNTS -> Web3JsonRPC.RequestAccounts()
        PERSONAL_SIGN -> params.toPersonalSignCoinbase()
        ETH_SIGN_TYPED_DATA_V3 -> params.toSignTypedDataV3()
        ETH_SIGN_TYPED_DATA_V4 -> params.toSignTypedDataV4()
        ETH_SIGN_TRANSACTION -> params.toEthSignTransaction(chainId)
        ETH_SEND_TRANSACTION -> params.toEthSendTransaction(chainId)
        WALLET_ADD_ETHEREUM_CHAIN -> params.toAddEthChain()
        WALLET_SWITCH_ETHEREUM_CHAIN -> params.toSwitchEthChain()
        WALLET_WATCH_ASSET -> params.toWalletWatchAssets()
        else -> throw Throwable("Unhandled method")
    }

    fun getAccount() {
        coinbaseWalletSDK.isConnected
        coinbaseWalletSDK
    }

    fun isInstalled() = coinbaseWalletSDK.isCoinbaseWalletInstalled

    fun isLauncherSet() = activityLauncher.launcher != null

    internal class ActivityResultLauncherHolder<I> {
        var launcher: ActivityResultLauncher<I>? = null

        fun launch(input: I?, options: ActivityOptionsCompat? = null) {
            launcher?.launch(input, options) ?: error("Launcher has not been initialized")
        }

        fun unregister() {
            launcher?.unregister()
            launcher = null
        }
    }
}
