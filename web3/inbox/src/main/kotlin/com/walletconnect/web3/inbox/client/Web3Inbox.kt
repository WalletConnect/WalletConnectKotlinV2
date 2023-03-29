package com.walletconnect.web3.inbox.client

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatClient
import com.walletconnect.web3.inbox.chat.ChatEventHandler
import com.walletconnect.web3.inbox.di.jsonRpcModule
import com.walletconnect.web3.inbox.di.proxyModule
import com.walletconnect.web3.inbox.ui.Web3InboxState
import com.walletconnect.web3.inbox.ui.Web3InboxView
import com.walletconnect.web3.inbox.ui.rememberWebViewState
import com.walletconnect.web3.inbox.webview.WebViewPresenter

object Web3Inbox {
    private var isClientInitialized = false
    private lateinit var account: LateInitAccountId
    private lateinit var chatEventHandler: ChatEventHandler

    @Throws(IllegalStateException::class)
    fun initialize(init: Inbox.Params.Init, onError: (Inbox.Model.Error) -> Unit) {
        ChatClient.initialize(Chat.Params.Init(init.core)) { error -> onError(Inbox.Model.Error(error.throwable)) }

        runCatching {
            account = LateInitAccountId(AccountId(init.account.value))
            wcKoinApp.run { modules(jsonRpcModule(), proxyModule(ChatClient, init.onSign, ::onPageFinished)) }
            ChatClient.setChatDelegate(wcKoinApp.koin.get<ChatEventHandler>())
            isClientInitialized = true
        }.onFailure { e -> onError(Inbox.Model.Error(e)) }
    }

    @Composable
    fun rememberWeb3InboxState(): Web3InboxState = wrapComposableWithInitializationCheck {
        val webViewPresenter = wcKoinApp.koin.get<WebViewPresenter>()
        Web3InboxState(rememberWebViewState(webViewPresenter.web3InboxUrl(account.value), queryParams = webViewPresenter.web3InboxUrlQueryParams(account.value)))
    }

    @Composable
    @Throws(IllegalStateException::class)
    fun View(modifier: Modifier = Modifier, state: Web3InboxState) = wrapComposableWithInitializationCheck {
        Web3InboxView(modifier, wcKoinApp.koin.get(), state)
    }

    private fun onPageFinished() = wrapWithInitializationCheck {
        chatEventHandler = wcKoinApp.koin.get()
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

    private data class LateInitAccountId(
        val value: AccountId,
    )
}
