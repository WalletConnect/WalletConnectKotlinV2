@file:JvmSynthetic

package com.walletconnect.web3.inbox.chat.di

import com.walletconnect.chat.client.ChatInterface
import com.walletconnect.web3.inbox.chat.event.ChatEventHandler
import com.walletconnect.web3.inbox.chat.event.OnInviteAcceptedChatEventUseCase
import com.walletconnect.web3.inbox.chat.event.OnInviteChatEventUseCase
import com.walletconnect.web3.inbox.chat.event.OnInviteRejectedChatEventUseCase
import com.walletconnect.web3.inbox.chat.event.OnLeftChatEventUseCase
import com.walletconnect.web3.inbox.chat.event.OnMessageChatEventUseCase
import com.walletconnect.web3.inbox.chat.event.OnSyncUpdateChatEventUseCase
import com.walletconnect.web3.inbox.chat.request.AcceptInviteRequestUseCase
import com.walletconnect.web3.inbox.chat.request.ChatProxyRequestHandler
import com.walletconnect.web3.inbox.chat.request.GetMessagesRequestUseCase
import com.walletconnect.web3.inbox.chat.request.GetReceivedInvitesRequestUseCase
import com.walletconnect.web3.inbox.chat.request.GetSentInvitesRequestUseCase
import com.walletconnect.web3.inbox.chat.request.GetThreadsRequestUseCase
import com.walletconnect.web3.inbox.chat.request.InviteRequestUseCase
import com.walletconnect.web3.inbox.chat.request.MessageRequestUseCase
import com.walletconnect.web3.inbox.chat.request.RegisterRequestUseCase
import com.walletconnect.web3.inbox.chat.request.RejectInviteRequestUseCase
import com.walletconnect.web3.inbox.chat.request.ResolveRequestUseCase
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.common.proxy.ChatProxyInteractor
import org.koin.dsl.module

@JvmSynthetic
internal fun chatProxyModule(
    chatClient: ChatInterface,
    onSign: (message: String) -> Inbox.Model.Cacao.Signature,
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
