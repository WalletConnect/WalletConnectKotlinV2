@file:JvmSynthetic

package com.walletconnect.web3.inbox.push.di

import com.walletconnect.utils.addDeserializerEntry
import com.walletconnect.utils.addJsonAdapter
import com.walletconnect.utils.addSerializerEntry
import com.walletconnect.web3.inbox.json_rpc.Web3InboxMethods
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPCCallPushSubscriptionJsonAdapter
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPCCallPushUpdateJsonAdapter
import org.koin.dsl.module

@JvmSynthetic
internal fun pushJsonRpcModule() = module {

    addSerializerEntry(Web3InboxRPC.Request.Push.Subscribe::class)
    addSerializerEntry(Web3InboxRPC.Request.Push.Update::class)
    addSerializerEntry(Web3InboxRPC.Request.Push.DeleteSubscription::class)
    addSerializerEntry(Web3InboxRPC.Request.Push.GetActiveSubscriptions::class)
    addSerializerEntry(Web3InboxRPC.Request.Push.GetMessageHistory::class)
    addSerializerEntry(Web3InboxRPC.Request.Push.DeletePushMessage::class)
    addSerializerEntry(Web3InboxRPC.Request.Push.EnableSync::class)

    addSerializerEntry(Web3InboxRPC.Call.Notify.Subscription::class)
    addSerializerEntry(Web3InboxRPC.Call.Notify.Update::class)
    addSerializerEntry(Web3InboxRPC.Call.Notify.Delete::class)
    addSerializerEntry(Web3InboxRPC.Call.Notify.Message::class)

    addDeserializerEntry(Web3InboxMethods.Request.Push.SUBSCRIBE, Web3InboxRPC.Request.Push.Subscribe::class)
    addDeserializerEntry(Web3InboxMethods.Request.Push.UPDATE, Web3InboxRPC.Request.Push.Update::class)
    addDeserializerEntry(Web3InboxMethods.Request.Push.DELETE_SUBSCRIPTION, Web3InboxRPC.Request.Push.DeleteSubscription::class)
    addDeserializerEntry(Web3InboxMethods.Request.Push.GET_ACTIVE_SUBSCRIPTIONS, Web3InboxRPC.Request.Push.GetActiveSubscriptions::class)
    addDeserializerEntry(Web3InboxMethods.Request.Push.GET_MESSAGE_HISTORY, Web3InboxRPC.Request.Push.GetMessageHistory::class)
    addDeserializerEntry(Web3InboxMethods.Request.Push.DELETE_PUSH_MESSAGE, Web3InboxRPC.Request.Push.DeletePushMessage::class)
    addDeserializerEntry(Web3InboxMethods.Request.Push.ENABLE_SYNC, Web3InboxRPC.Request.Push.EnableSync::class)

    addDeserializerEntry(Web3InboxMethods.Call.Notify.SUBSCRIPTION, Web3InboxRPC.Call.Notify.Subscription::class)
    addDeserializerEntry(Web3InboxMethods.Call.Notify.UPDATE, Web3InboxRPC.Call.Notify.Update::class)
    addDeserializerEntry(Web3InboxMethods.Call.Notify.DELETE, Web3InboxRPC.Call.Notify.Delete::class)
    addDeserializerEntry(Web3InboxMethods.Call.Notify.MESSAGE, Web3InboxRPC.Call.Notify.Message::class)

    addJsonAdapter(Web3InboxRPC.Call.Notify.Subscription::class.java, ::Web3InboxRPCCallNotifySubscriptionJsonAdapter)
    addJsonAdapter(Web3InboxRPC.Call.Notify.Update::class.java, ::Web3InboxRPCCallPushUpdateJsonAdapter)
}