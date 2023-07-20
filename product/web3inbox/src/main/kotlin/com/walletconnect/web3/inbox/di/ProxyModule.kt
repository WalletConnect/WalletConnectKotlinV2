@file:JvmSynthetic

package com.walletconnect.web3.inbox.di

import com.walletconnect.chat.client.ChatInterface
import com.walletconnect.push.client.PushWalletInterface
import com.walletconnect.web3.inbox.chat.di.chatProxyModule
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.client.toCommon
import com.walletconnect.web3.inbox.common.proxy.ProxyRequestHandler
import com.walletconnect.web3.inbox.push.di.pushProxyModule
import com.walletconnect.web3.inbox.sync.di.syncProxyModule
import com.walletconnect.web3.inbox.webview.WebViewPresenter
import com.walletconnect.web3.inbox.webview.WebViewWeakReference
import org.koin.dsl.module

@JvmSynthetic
internal fun proxyModule(
    chatClient: ChatInterface,
    pushWalletClient: PushWalletInterface,
    onSign: (message: String) -> Inbox.Model.Cacao.Signature,
    config: Inbox.Model.Config,
) = module {
    includes(
        pushProxyModule(pushWalletClient, onSign),
        chatProxyModule(chatClient, onSign),
        syncProxyModule()
    )

    single { config.toCommon() }
    single { WebViewWeakReference() }
    single { ProxyRequestHandler(get(), get(), get()) }
    single { WebViewPresenter(get(), get(), get(), get()) }
}
