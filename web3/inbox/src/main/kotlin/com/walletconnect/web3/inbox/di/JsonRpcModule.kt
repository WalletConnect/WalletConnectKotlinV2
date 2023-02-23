@file:JvmSynthetic

package com.walletconnect.web3.inbox.di

import com.walletconnect.utils.addDeserializerEntry
import com.walletconnect.utils.addSerializerEntry
import com.walletconnect.web3.inbox.json_rpc.Web3InboxMethods
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import org.koin.dsl.module

@JvmSynthetic
internal fun jsonRpcModule() = module {
    addSerializerEntry(Web3InboxRPC.Request.Register::class)
    addSerializerEntry(Web3InboxRPC.Request.GetReceivedInvites::class)
    addSerializerEntry(Web3InboxRPC.Request.GetSentInvites::class)
    addSerializerEntry(Web3InboxRPC.Request.GetThreads::class)
    addSerializerEntry(Web3InboxRPC.Request.GetMessages::class)
    addSerializerEntry(Web3InboxRPC.Request.Resolve::class)
    addSerializerEntry(Web3InboxRPC.Request.Accept::class)
    addSerializerEntry(Web3InboxRPC.Request.Reject::class)
    addSerializerEntry(Web3InboxRPC.Request.Message::class)
    addSerializerEntry(Web3InboxRPC.Request.Invite::class)

    addSerializerEntry(Web3InboxRPC.Call.SetAccount::class)
    addSerializerEntry(Web3InboxRPC.Call.Chat.Invite::class)
    addSerializerEntry(Web3InboxRPC.Call.Chat.Message::class)
    addSerializerEntry(Web3InboxRPC.Call.Chat.InviteAccepted::class)
    addSerializerEntry(Web3InboxRPC.Call.Chat.InviteRejected::class)
    addSerializerEntry(Web3InboxRPC.Call.Chat.Leave::class)

    addDeserializerEntry(Web3InboxMethods.Request.REGISTER, Web3InboxRPC.Request.Register::class)
    addDeserializerEntry(Web3InboxMethods.Request.GET_RECEIVED_INVITES, Web3InboxRPC.Request.GetReceivedInvites::class)
    addDeserializerEntry(Web3InboxMethods.Request.GET_SENT_INVITES, Web3InboxRPC.Request.GetSentInvites::class)
    addDeserializerEntry(Web3InboxMethods.Request.GET_THREADS, Web3InboxRPC.Request.GetThreads::class)
    addDeserializerEntry(Web3InboxMethods.Request.GET_MESSAGES, Web3InboxRPC.Request.GetMessages::class)
    addDeserializerEntry(Web3InboxMethods.Request.RESOLVE, Web3InboxRPC.Request.Resolve::class)
    addDeserializerEntry(Web3InboxMethods.Request.ACCEPT, Web3InboxRPC.Request.Accept::class)
    addDeserializerEntry(Web3InboxMethods.Request.REJECT, Web3InboxRPC.Request.Reject::class)
    addDeserializerEntry(Web3InboxMethods.Request.MESSAGE, Web3InboxRPC.Request.Message::class)
    addDeserializerEntry(Web3InboxMethods.Request.INVITE, Web3InboxRPC.Request.Invite::class)

    addDeserializerEntry(Web3InboxMethods.Call.SET_ACCOUNT, Web3InboxRPC.Call.SetAccount::class)
    addDeserializerEntry(Web3InboxMethods.Call.Chat.INVITE, Web3InboxRPC.Call.Chat.Invite::class)
    addDeserializerEntry(Web3InboxMethods.Call.Chat.MESSAGE, Web3InboxRPC.Call.Chat.Message::class)
    addDeserializerEntry(Web3InboxMethods.Call.Chat.INVITE_ACCEPTED, Web3InboxRPC.Call.Chat.InviteAccepted::class)
    addDeserializerEntry(Web3InboxMethods.Call.Chat.INVITE_REJECTED, Web3InboxRPC.Call.Chat.InviteRejected::class)
    addDeserializerEntry(Web3InboxMethods.Call.Chat.LEAVE, Web3InboxRPC.Call.Chat.Leave::class)
}