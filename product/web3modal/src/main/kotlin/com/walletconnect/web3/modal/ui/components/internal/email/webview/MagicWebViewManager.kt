package com.walletconnect.web3.modal.ui.components.internal.email.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.MutableContextWrapper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.LinearLayout
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.modal.domain.magic.model.MagicEvent
import com.walletconnect.web3.modal.domain.magic.model.MagicEvent.ConnectEmailResult
import com.walletconnect.web3.modal.domain.magic.model.MagicEvent.ConnectOTPResult
import com.walletconnect.web3.modal.domain.magic.model.MagicEvent.GetUserResult
import com.walletconnect.web3.modal.domain.magic.model.MagicEvent.RpcRequestResult
import com.walletconnect.web3.modal.domain.magic.model.MagicEvent.SessionUpdate
import com.walletconnect.web3.modal.domain.magic.model.MagicEvent.SignOutSuccess
import com.walletconnect.web3.modal.domain.magic.model.MagicEvent.SwitchNetworkResult
import com.walletconnect.web3.modal.domain.magic.model.MagicEvent.SyncDappDataSuccess
import com.walletconnect.web3.modal.domain.magic.model.MagicEvent.SyncThemeSuccess
import com.walletconnect.web3.modal.domain.magic.model.MagicRequest
import com.walletconnect.web3.modal.domain.magic.model.MagicTypeResponse
import com.walletconnect.web3.modal.ui.utils.WEB_APP_INTERFACE
import com.walletconnect.web3.modal.ui.utils.sendMethod
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject

//const val SECURE_WEBSITE_URL = "https://192.168.0.220"
const val SECURE_WEBSITE_URL = "https://secure-mobile.walletconnect.com"

internal class MagicWebViewManager(
    private val headers: Map<String, String>,
    private val projectId: ProjectId,
    private val appData: AppMetaData,
    private val bundleId: String,
    private val logger: Logger,
) {
    private var mutableContext: MutableContextWrapper? = null
    private var webView: WebView? = null

    private val _eventFlow = MutableSharedFlow<MagicEvent>()
    val eventFlow: SharedFlow<MagicEvent>
        get() = _eventFlow.asSharedFlow()

    internal fun updateWebView(activity: Activity) {
        if (mutableContext == null) {
            mutableContext = MutableContextWrapper(activity)
            webView = WebView(mutableContext!!)

            WebView.setWebContentsDebuggingEnabled(true)
            initWebView()
        } else {
            mutableContext?.baseContext = activity
        }
    }

    @SuppressLint("JavascriptInterface", "SetJavaScriptEnabled")
    private fun initWebView() {
        // Setup settings
        val webSettings = webView?.settings
        webSettings?.javaScriptEnabled = true
        webSettings?.domStorageEnabled = true

        // Setup webView
        webView?.apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            addJavascriptInterface(WebAppInterface(), WEB_APP_INTERFACE)
            webChromeClient = EmailMagicChromeClient(logger)
            webViewClient = EmailMagicWebViewClient(appData, projectId, logger)

            loadUrl(SECURE_WEBSITE_URL + "/mobile-sdk?projectId=${projectId.value}&bundleId=$bundleId", headers)
        }
    }

    inner class WebAppInterface {

        @JavascriptInterface
        fun postMessage(message: String) {
            logger.log("postMessage $message")

            try {
                val jsonObject = JSONObject(message)
                if (jsonObject.has("msgType")) {
                    return
                }

                if (jsonObject.has("data") && jsonObject.getJSONObject("data").has("type")) {
                    logger.log("has type, parse: $jsonObject")
                    val event = parseMessage(jsonObject) ?: return
                    logger.log("Event $event")
                    consumeEvent(event)

                    logger.log("PostMessage event: $event")
                    _eventFlow.tryEmit(event)
                    return
                }
            } catch (e: Throwable) {
                logger.error("error: $e")
            }
        }
    }

    private fun consumeEvent(event: MagicEvent) {
        when (event) {
            // todo update sessions at specifics events
            is MagicEvent.IsConnectResult.IsConnectedSuccess -> {
                println("connected success")
            }

            MagicEvent.IsConnectResult.IsConnectedError -> TODO()
            ConnectEmailResult.ConnectEmailError -> TODO()
            is ConnectEmailResult.ConnectEmailSuccess -> TODO()
            is ConnectOTPResult.ConnectOTPError -> TODO()
            ConnectOTPResult.ConnectOTPSuccess -> TODO()
            GetUserResult.GetUserError -> TODO()
            is GetUserResult.GetUserSuccess -> TODO()
            is RpcRequestResult.RpcRequestError -> TODO()
            is RpcRequestResult.RpcRequestSuccess -> TODO()
            is SessionUpdate -> TODO()
            SignOutSuccess -> TODO()
            SwitchNetworkResult.SwitchNetworkError -> TODO()
            is SwitchNetworkResult.SwitchNetworkSuccess -> TODO()
            SyncDappDataSuccess -> TODO()
            SyncThemeSuccess -> TODO()
            else -> Unit
        }
    }

    fun sendMessage(request: MagicRequest) {
        webView?.sendMethod(request)
    }

    // TODO: Migrate parsing to Moshi adapter later
    private fun parseMessage(jsonObject: JSONObject): MagicEvent? {
        val dataJSONObject = jsonObject.getJSONObject("data")
        return when (dataJSONObject.getString("type")) {
            MagicTypeResponse.IS_CONNECTED_SUCCESS.value -> MagicEvent.IsConnectResult.IsConnectedSuccess(dataJSONObject.getJSONObject("payload").getBoolean("isConnected"))
            MagicTypeResponse.IS_CONNECTED_ERROR.value -> MagicEvent.IsConnectResult.IsConnectedError
            MagicTypeResponse.SYNC_THEME_SUCCESS.value -> SyncThemeSuccess
            MagicTypeResponse.SYNC_DAPP_DATA_SUCCESS.value -> SyncDappDataSuccess
            MagicTypeResponse.CONNECT_EMAIL_SUCCESS.value -> ConnectEmailResult.ConnectEmailSuccess(jsonObject.getJSONObject("payload").getString("action"))
            MagicTypeResponse.CONNECT_EMAIL_ERROR.value -> ConnectEmailResult.ConnectEmailError
            MagicTypeResponse.CONNECT_OTP_SUCCESS.value -> ConnectOTPResult.ConnectOTPSuccess
            MagicTypeResponse.CONNECT_OTP_ERROR.value -> ConnectOTPResult.ConnectOTPError
            MagicTypeResponse.GET_USER_SUCCESS.value -> {
                val payload = jsonObject.getJSONObject("payload")
                GetUserResult.GetUserSuccess(payload.getString("email"), payload.getString("address"), payload.getInt("chainId"))
            }

            MagicTypeResponse.GET_USER_ERROR.value -> GetUserResult.GetUserError
            MagicTypeResponse.SESSION_UPDATE.value -> SessionUpdate(jsonObject.getJSONObject("payload").getString("token"))
            MagicTypeResponse.SWITCH_NETWORK_SUCCESS.value -> SwitchNetworkResult.SwitchNetworkSuccess(jsonObject.getJSONObject("payload").getInt("chainId"))
            MagicTypeResponse.SWITCH_NETWORK_ERROR.value -> SwitchNetworkResult.SwitchNetworkError
            MagicTypeResponse.RPC_REQUEST_SUCCESS.value -> RpcRequestResult.RpcRequestSuccess(jsonObject.getJSONObject("payload").getString("hash"))
            MagicTypeResponse.RPC_REQUEST_ERROR.value -> RpcRequestResult.RpcRequestError(jsonObject.getJSONObject("payload").getString("error"))
            MagicTypeResponse.SIGN_OUT_SUCCESS.value -> SignOutSuccess
            else -> null
        }
    }
}