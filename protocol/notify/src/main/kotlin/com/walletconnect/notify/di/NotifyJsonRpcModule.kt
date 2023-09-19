@file:JvmSynthetic

package com.walletconnect.notify.di

import com.walletconnect.notify.common.JsonRpcMethod
import com.walletconnect.notify.common.model.NotifyRpc
import com.walletconnect.utils.addDeserializerEntry
import com.walletconnect.utils.addSerializerEntry
import org.koin.dsl.module

@JvmSynthetic
internal fun notifyJsonRpcModule() = module {

    addSerializerEntry(NotifyRpc.NotifyMessage::class)
    addSerializerEntry(NotifyRpc.NotifyDelete::class)
    addSerializerEntry(NotifyRpc.NotifySubscribe::class)
    addSerializerEntry(NotifyRpc.NotifyUpdate::class)
    addSerializerEntry(NotifyRpc.NotifyWatchSubscriptions::class)
    addSerializerEntry(NotifyRpc.NotifySubscriptionsChanged::class)

    addDeserializerEntry(JsonRpcMethod.WC_NOTIFY_MESSAGE, NotifyRpc.NotifyMessage::class)
    addDeserializerEntry(JsonRpcMethod.WC_NOTIFY_DELETE, NotifyRpc.NotifyDelete::class)
    addDeserializerEntry(JsonRpcMethod.WC_NOTIFY_SUBSCRIBE, NotifyRpc.NotifySubscribe::class)
    addDeserializerEntry(JsonRpcMethod.WC_NOTIFY_UPDATE, NotifyRpc.NotifyUpdate::class)
    addDeserializerEntry(JsonRpcMethod.WC_NOTIFY_WATCH_SUBSCRIPTIONS, NotifyRpc.NotifyWatchSubscriptions::class)
    addDeserializerEntry(JsonRpcMethod.WC_NOTIFY_SUBSCRIPTIONS_CHANGED, NotifyRpc.NotifySubscriptionsChanged::class)
}