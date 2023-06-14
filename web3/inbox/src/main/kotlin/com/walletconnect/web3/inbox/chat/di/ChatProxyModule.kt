@file:JvmSynthetic

package com.walletconnect.web3.inbox.chat.di

import com.walletconnect.chat.client.ChatInterface
import com.walletconnect.web3.inbox.chat.event.ChatEventHandler
import com.walletconnect.web3.inbox.chat.request.ChatProxyRequestHandler
import com.walletconnect.web3.inbox.chat.event.*
import com.walletconnect.web3.inbox.chat.request.*
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.common.proxy.ChatProxyInteractor
import org.koin.dsl.module

@JvmSynthetic
internal fun chatProxyModule(
    chatClient: ChatInterface,
    onSign: (message: String) -> Inbox.Model.Cacao.Signature,
    onPageFinished: () -> Unit,
) = module {
    single { ChatProxyInteractor(get(), get()) }

    single { RegisterRequestUseCase(chatClient, get(), onSign) }
    single { GetReceivedInvitesRequestUseCase(chatClient, get()) }
    single { GetSentInvitesRequestUseCase(chatClient, get()) }
    single { GetThreadsRequestUseCase(chatClient, get()) }
    single { GetMessagesRequestUseCase(chatClient, get()) }
    single { AcceptInviteRequestUseCase(chatClient, get()) }
    single { RejectInviteRequestUseCase(chatClient, get()) }
    single { ResolveRequestUseCase(chatClient, get()) }
    single { MessageRequestUseCase(chatClient, get()) }
    single { InviteRequestUseCase(chatClient, get()) }

    single { OnInviteChatEventUseCase(get()) }
    single { OnMessageChatEventUseCase(get()) }
    single { OnInviteAcceptedChatEventUseCase(get()) }
    single { OnInviteRejectedChatEventUseCase(get()) }
    single { OnLeftChatEventUseCase(get()) }
    single { OnSyncUpdateChatEventUseCase(get()) }

    single { ChatEventHandler(get(), get(), get(), get(), get(), get()) }

    single { ChatProxyRequestHandler(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}
