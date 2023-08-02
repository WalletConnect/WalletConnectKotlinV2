@file:JvmSynthetic

package com.walletconnect.notify.common.model

import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.params.NotifyParams
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.notify.client.Notify

@JvmSynthetic
internal fun NotifyParams.MessageParams.toEngineDO(): EngineDO.Message =
    EngineDO.Message(title, body, icon, url, type)

@JvmSynthetic
internal fun EngineDO.Message.toWalletClient(): Notify.Model.Message {
    return Notify.Model.Message(title, body, icon, url, type)
}

@JvmSynthetic
internal fun EngineDO.Record.toWalletClient(): Notify.Model.MessageRecord {
    return Notify.Model.MessageRecord(
        id = this.id.toString(),
        topic = this.topic,
        publishedAt = this.publishedAt,
        message = Notify.Model.Message(
            title = this.message.title,
            body = this.message.body,
            icon = this.message.icon,
            url = this.message.url,
            type = this.message.type
        )
    )
}


@JvmSynthetic
internal fun ((String) -> Notify.Model.Cacao.Signature?).toWalletClient(): (String) -> Cacao.Signature? = { message ->
    this(message)?.let { publicCacaoSignature: Notify.Model.Cacao.Signature ->
        Cacao.Signature(publicCacaoSignature.t, publicCacaoSignature.s, publicCacaoSignature.m)
    }
}

@JvmSynthetic
internal fun EngineDO.Delete.toWalletClient(): Notify.Event.Delete {
    return Notify.Event.Delete(topic)
}

@JvmSynthetic
internal fun EngineDO.Subscription.Active.toEvent(): Notify.Event.Subscription.Result {
    return Notify.Event.Subscription.Result(
        Notify.Model.Subscription(
            topic = notifyTopic.value,
            account = account.value,
            relay = relay.toClient(),
            metadata = dappMetaData.toClient(),
            scope = mapOfScope.toClient(),
            expiry = expiry.seconds,
        )
    )
}

@JvmSynthetic
internal fun EngineDO.Subscription.Active.toModel(): Notify.Model.Subscription {
    return Notify.Model.Subscription(
        topic = notifyTopic.value,
        account = account.value,
        relay = relay.toClient(),
        metadata = dappMetaData.toClient(),
        scope = mapOfScope.toClient(),
        expiry = expiry.seconds,
    )
}

@JvmSynthetic
internal fun EngineDO.Subscription.Error.toWalletClient(): Notify.Event.Subscription.Error {
    return Notify.Event.Subscription.Error(requestId, rejectionReason)
}

@JvmSynthetic
internal fun EngineDO.Update.Result.toWalletClient(): Notify.Event.Update.Result {
    return Notify.Event.Update.Result(
        Notify.Model.Subscription(
            topic = notifyTopic.value,
            account = account.value,
            relay = relay.toClient(),
            metadata = dappMetaData.toClient(),
            scope = mapOfScope.toClient(),
            expiry = expiry.seconds,
        )
    )
}

@JvmSynthetic
internal fun EngineDO.Update.Error.toWalletClient(): Notify.Event.Update.Error {
    return Notify.Event.Update.Error(requestId, rejectionReason)
}

@JvmSynthetic
internal fun RelayProtocolOptions.toClient(): Notify.Model.Subscription.Relay {
    return Notify.Model.Subscription.Relay(protocol, data)
}

@JvmSynthetic
internal fun Map<String, EngineDO.Scope.Cached>.toClient(): Map<Notify.Model.Subscription.ScopeName, Notify.Model.Subscription.ScopeSetting> {
    return map { (key, value) ->
        Notify.Model.Subscription.ScopeName(key) to Notify.Model.Subscription.ScopeSetting(value.description, value.isSelected)
    }.toMap()
}

@JvmSynthetic
internal fun SDKError.toClient(): Notify.Model.Error {
    return Notify.Model.Error(exception)
}

@JvmSynthetic
internal fun Map<String, EngineDO.Scope.Cached>.toDb(): Map<String, Pair<String, Boolean>> =
    mapValues { (_, value) -> Pair(value.description, true) }