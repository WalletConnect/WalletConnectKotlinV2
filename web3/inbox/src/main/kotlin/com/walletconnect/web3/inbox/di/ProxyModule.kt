@file:JvmSynthetic

package com.walletconnect.web3.inbox.di

import com.walletconnect.chat.client.ChatInterface
import com.walletconnect.web3.inbox.chat.ChatEventHandler
import com.walletconnect.web3.inbox.chat.event.*
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.proxy.ProxyInteractor
import com.walletconnect.web3.inbox.proxy.ProxyRequestHandler
import com.walletconnect.web3.inbox.proxy.request.*
import com.walletconnect.web3.inbox.webview.WebViewPresenter
import com.walletconnect.web3.inbox.webview.WebViewWeakReference
import org.koin.dsl.module

@JvmSynthetic
internal fun proxyModule(
    chatClient: ChatInterface,
    onSign: (message: String) -> Inbox.Model.Cacao.Signature,
    onPageFinished: () -> Unit,
) = module {
    single { WebViewWeakReference() }
    single { ProxyInteractor(get(), get()) }
    single { RegisterRequestUseCase(chatClient, get(), onSign) }
    single { GetReceivedInvitesRequestUseCase(chatClient, get()) }
    single { GetSentInvitesRequestUseCase(chatClient, get()) }
    single { GetThreadsRequestUseCase(chatClient, get()) }
    single { GetMessagesRequestUseCase(chatClient, get()) }
    single { AcceptRequestUseCase(chatClient, get()) }
    single { RejectRequestUseCase(chatClient, get()) }
    single { ResolveRequestUseCase(chatClient, get()) }
    single { MessageRequestUseCase(chatClient, get()) }
    single { InviteRequestUseCase(chatClient, get()) }
    single { ProxyRequestHandler(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { WebViewPresenter(get(), get(), get(), onPageFinished) }

    single { OnInviteChatEventUseCase(get()) }
    single { OnMessageChatEventUseCase(get()) }
    single { OnInviteAcceptedChatEventUseCase(get()) }
    single { OnInviteRejectedChatEventUseCase(get()) }
    single { OnLeftChatEventUseCase(get()) }

    single { ChatEventHandler(get(), get(), get(), get(), get(), get()) }
}
