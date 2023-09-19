@file:JvmSynthetic

package com.walletconnect.web3.inbox.notify.di

import com.walletconnect.utils.addDeserializerEntry
import com.walletconnect.utils.addJsonAdapter
import com.walletconnect.utils.addSerializerEntry
import com.walletconnect.web3.inbox.json_rpc.Web3InboxMethods
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPCCallNotifySubscriptionJsonAdapter
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPCCallNotifyUpdateJsonAdapter
import org.koin.dsl.module

@JvmSynthetic
internal fun notifyJsonRpcModule() = module {

    addSerializerEntry(Web3InboxRPC.Request.Notify.Subscribe::class)
    addSerializerEntry(Web3InboxRPC.Request.Notify.Update::class)
    addSerializerEntry(Web3InboxRPC.Request.Notify.DeleteSubscription::class)
    addSerializerEntry(Web3InboxRPC.Request.Notify.GetActiveSubscriptions::class)
    addSerializerEntry(Web3InboxRPC.Request.Notify.GetMessageHistory::class)
    addSerializerEntry(Web3InboxRPC.Request.Notify.DeleteNotifyMessage::class)
    addSerializerEntry(Web3InboxRPC.Request.Notify.Register::class)

    addSerializerEntry(Web3InboxRPC.Call.Notify.Subscription::class)
    addSerializerEntry(Web3InboxRPC.Call.Notify.Update::class)
    addSerializerEntry(Web3InboxRPC.Call.Notify.Delete::class)
    addSerializerEntry(Web3InboxRPC.Call.Notify.SubscriptionsChanged::class)
    addSerializerEntry(Web3InboxRPC.Call.Notify.Message::class)

    addDeserializerEntry(Web3InboxMethods.Request.Notify.SUBSCRIBE, Web3InboxRPC.Request.Notify.Subscribe::class)
    addDeserializerEntry(Web3InboxMethods.Request.Notify.UPDATE, Web3InboxRPC.Request.Notify.Update::class)
    addDeserializerEntry(Web3InboxMethods.Request.Notify.DELETE_SUBSCRIPTION, Web3InboxRPC.Request.Notify.DeleteSubscription::class)
    addDeserializerEntry(Web3InboxMethods.Request.Notify.GET_ACTIVE_SUBSCRIPTIONS, Web3InboxRPC.Request.Notify.GetActiveSubscriptions::class)
    addDeserializerEntry(Web3InboxMethods.Request.Notify.GET_MESSAGE_HISTORY, Web3InboxRPC.Request.Notify.GetMessageHistory::class)
    addDeserializerEntry(Web3InboxMethods.Request.Notify.DELETE_NOTIFY_MESSAGE, Web3InboxRPC.Request.Notify.DeleteNotifyMessage::class)
    addDeserializerEntry(Web3InboxMethods.Request.Notify.REGISTER, Web3InboxRPC.Request.Notify.Register::class)

    addDeserializerEntry(Web3InboxMethods.Call.Notify.SUBSCRIPTION, Web3InboxRPC.Call.Notify.Subscription::class)
    addDeserializerEntry(Web3InboxMethods.Call.Notify.UPDATE, Web3InboxRPC.Call.Notify.Update::class)
    addDeserializerEntry(Web3InboxMethods.Call.Notify.DELETE, Web3InboxRPC.Call.Notify.Delete::class)
    addDeserializerEntry(Web3InboxMethods.Call.Notify.SUBSCRIPTIONS_CHANGED, Web3InboxRPC.Call.Notify.SubscriptionsChanged::class)
    addDeserializerEntry(Web3InboxMethods.Call.Notify.MESSAGE, Web3InboxRPC.Call.Notify.Message::class)

    addJsonAdapter(Web3InboxRPC.Call.Notify.Subscription::class.java, ::Web3InboxRPCCallNotifySubscriptionJsonAdapter)
    addJsonAdapter(Web3InboxRPC.Call.Notify.Update::class.java, ::Web3InboxRPCCallNotifyUpdateJsonAdapter)
}