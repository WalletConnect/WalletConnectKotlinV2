@file:JvmSynthetic

package com.walletconnect.web3.inbox.di

import com.walletconnect.chat.client.ChatInterface
import com.walletconnect.push.wallet.client.PushWalletInterface
import com.walletconnect.web3.inbox.chat.ChatEventHandler
import com.walletconnect.web3.inbox.chat.event.*
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.proxy.ChatProxyInteractor
import com.walletconnect.web3.inbox.proxy.ProxyRequestHandler
import com.walletconnect.web3.inbox.proxy.PushProxyInteractor
import com.walletconnect.web3.inbox.proxy.request.*
import com.walletconnect.web3.inbox.push.GetActiveSubscriptionsRequestUseCase
import com.walletconnect.web3.inbox.push.PushEventHandler
import com.walletconnect.web3.inbox.push.event.OnRequestPushEventUseCase
import com.walletconnect.web3.inbox.webview.WebViewPresenter
import com.walletconnect.web3.inbox.webview.WebViewWeakReference
import org.koin.dsl.module

//todo split or refactor proxy and events usecases. split into chat and push as well
@JvmSynthetic
internal fun proxyModule(
    chatClient: ChatInterface,
    pushWalletClient: PushWalletInterface,
    onSign: (message: String) -> Inbox.Model.Cacao.Signature,
    onPageFinished: () -> Unit,
) = module {
    single { WebViewWeakReference() }
    single { ChatProxyInteractor(get(), get()) }
    single { PushProxyInteractor(get(), get()) }

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
    single { ProxyRequestHandler(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { WebViewPresenter(get(), get(), get(), onPageFinished) }

    single { OnInviteChatEventUseCase(get()) }
    single { OnMessageChatEventUseCase(get()) }
    single { OnInviteAcceptedChatEventUseCase(get()) }
    single { OnInviteRejectedChatEventUseCase(get()) }
    single { OnLeftChatEventUseCase(get()) }

    single { ChatEventHandler(get(), get(), get(), get(), get(), get()) }

    single { GetActiveSubscriptionsRequestUseCase(pushWalletClient, get()) }

    single { OnRequestPushEventUseCase(get()) }

    single { PushEventHandler(get(), get()) }

}
