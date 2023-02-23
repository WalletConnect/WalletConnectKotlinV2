package com.walletconnect.web3.inbox.client

import android.content.Context
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatClient
import com.walletconnect.foundation.util.Logger
import com.walletconnect.util.generateId
import com.walletconnect.web3.inbox.chat.ChatEventHandler
import com.walletconnect.web3.inbox.common.model.AccountId
import com.walletconnect.web3.inbox.di.jsonRpcModule
import com.walletconnect.web3.inbox.di.proxyModule
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.ProxyInteractor
import com.walletconnect.web3.inbox.ui.Web3InboxView
import com.walletconnect.web3.inbox.ui.WebViewState
import com.walletconnect.web3.inbox.ui.createWebView

object Web3Inbox {
    private var isClientInitialized = false
    private var webViewState: WebViewState = WebViewState.Loading
    private var account: AccountId? = null
    private lateinit var chatEventHandler: ChatEventHandler // todo: probably needs a host.

    @Throws(IllegalStateException::class)
    fun initialize(init: Inbox.Params.Init, onError: (Inbox.Model.Error) -> Unit) {
        ChatClient.initialize(Chat.Params.Init(init.core, init.keyServerUrl)) { error -> onError(Inbox.Model.Error(error.throwable)) }

        runCatching {
            account = AccountId(init.account.value)
            wcKoinApp.run { modules(jsonRpcModule(), proxyModule(ChatClient, init.onSign, ::onPageFinished)) }
            ChatClient.setChatDelegate(wcKoinApp.koin.get<ChatEventHandler>())
            isClientInitialized = true
        }.onFailure { e -> onError(Inbox.Model.Error(e)) }
    }

    @Composable
    @Throws(IllegalStateException::class)
    fun View(modifier: Modifier = Modifier) = wrapComposableWithInitializationCheck { Web3InboxView(modifier, wcKoinApp.koin.get(), webViewState) } //todo koin ugly

    fun getView(context: Context): WebView = wrapWithInitializationCheck { createWebView(context, wcKoinApp.koin.get(), webViewState) }

    private fun onPageFinished() = wrapWithInitializationCheck {
        if (webViewState is WebViewState.Loading) {
            wcKoinApp.koin.get<Logger>().log("onPageFinished")
            wcKoinApp.koin.get<ProxyInteractor>().call(Web3InboxRPC.Call.SetAccount(id = generateId(), params = Web3InboxParams.Call.SetAccountParams(account!!.address())))
            chatEventHandler = wcKoinApp.koin.get() // todo: probably needs a host. Starts listening for events
            webViewState = WebViewState.Initialized
        }
    }

    @Composable
    @Throws(IllegalStateException::class)
    private fun <R> wrapComposableWithInitializationCheck(block: @Composable () -> R): R {
        check(isClientInitialized) { "Web3Inbox needs to be initialized first using the initialize function" }
        return block()
    }

    @Throws(IllegalStateException::class)
    private fun <R> wrapWithInitializationCheck(block: () -> R): R {
        check(isClientInitialized) { "Web3Inbox needs to be initialized first using the initialize function" }
        return block()
    }
}
