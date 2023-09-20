@file:JvmSynthetic

package com.walletconnect.web3.inbox.di

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.chat.client.ChatInterface
import com.walletconnect.notify.client.NotifyInterface
import com.walletconnect.web3.inbox.chat.di.chatProxyModule
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.client.toCommon
import com.walletconnect.web3.inbox.common.proxy.ProxyRequestHandler
import com.walletconnect.web3.inbox.notify.di.notifyProxyModule
import com.walletconnect.web3.inbox.webview.WebViewPresenter
import com.walletconnect.web3.inbox.webview.WebViewWeakReference
import org.koin.dsl.module

@JvmSynthetic
internal fun proxyModule(
    chatClient: ChatInterface,
    notifyClient: NotifyInterface,
    onSign: (message: String) -> Inbox.Model.Cacao.Signature,
    config: Inbox.Model.Config,
    account: AccountId,
) = module {
    includes(
        notifyProxyModule(notifyClient, onSign, account),
        chatProxyModule(chatClient, onSign),
    )

    single { config.toCommon() }
    single { WebViewWeakReference() }
    single { ProxyRequestHandler(get(), get(), get()) }
    single { WebViewPresenter(get(), get(), get(), get()) }
}
