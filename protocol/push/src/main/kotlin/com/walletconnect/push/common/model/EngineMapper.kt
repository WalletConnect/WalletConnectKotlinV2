package com.walletconnect.push.common.model

import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.params.PushParams
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.push.client.Push

@JvmSynthetic
internal fun PushParams.MessageParams.toEngineDO(): EngineDO.PushMessage =
    EngineDO.PushMessage(title, body, icon, url, type)

@JvmSynthetic
internal fun EngineDO.PushProposal.toWalletClient(): Push.Event.Proposal {
    return Push.Event.Proposal(requestId, accountId.value, dappMetadata.toClient())
}

@JvmSynthetic
internal fun EngineDO.PushMessage.toWalletClient(): Push.Model.Message {
    return Push.Model.Message(title, body, icon, url, type)
}

@JvmSynthetic
internal fun EngineDO.PushRecord.toWalletClient(): Push.Model.MessageRecord {
    return Push.Model.MessageRecord(
        id = this.id.toString(),
        topic = this.topic,
        publishedAt = this.publishedAt,
        message = Push.Model.Message(
            title = this.message.title,
            body = this.message.body,
            icon = this.message.icon,
            url = this.message.url,
            type = this.message.type
        )
    )
}


@JvmSynthetic
internal fun ((String) -> Push.Model.Cacao.Signature?).toWalletClient(): (String) -> Cacao.Signature? = { message ->
    this(message)?.let { publicCacaoSignature: Push.Model.Cacao.Signature ->
        Cacao.Signature(publicCacaoSignature.t, publicCacaoSignature.s, publicCacaoSignature.m)
    }
}

@JvmSynthetic
internal fun EngineDO.PushDelete.toWalletClient(): Push.Event.Delete {
    return Push.Event.Delete(topic)
}

@JvmSynthetic
internal fun EngineDO.Subscription.Active.toEvent(): Push.Event.Subscription.Result {
    return Push.Event.Subscription.Result(
        Push.Model.Subscription(
            topic = pushTopic.value,
            account = account.value,
            relay = relay.toClient(),
            metadata = dappMetaData.toClient(),
            scope = mapOfScope.toClient(),
            expiry = expiry.seconds,
        )
    )
}

@JvmSynthetic
internal fun EngineDO.Subscription.Active.toModel(): Push.Model.Subscription {
    return Push.Model.Subscription(
        topic = pushTopic.value,
        account = account.value,
        relay = relay.toClient(),
        metadata = dappMetaData.toClient(),
        scope = mapOfScope.toClient(),
        expiry = expiry.seconds,
    )
}

@JvmSynthetic
internal fun EngineDO.Subscription.Error.toWalletClient(): Push.Event.Subscription.Error {
    return Push.Event.Subscription.Error(requestId, rejectionReason)
}

@JvmSynthetic
internal fun EngineDO.PushUpdate.Result.toWalletClient(): Push.Event.Update.Result {
    return Push.Event.Update.Result(
        Push.Model.Subscription(
            topic = pushTopic.value,
            account = account.value,
            relay = relay.toClient(),
            metadata = dappMetaData.toClient(),
            scope = mapOfScope.toClient(),
            expiry = expiry.seconds,
        )
    )
}

@JvmSynthetic
internal fun EngineDO.PushUpdate.Error.toWalletClient(): Push.Event.Update.Error {
    return Push.Event.Update.Error(requestId, rejectionReason)
}

@JvmSynthetic
internal fun EngineDO.PushLegacySubscription.toDappClient(): Push.Model.Subscription {
    return Push.Model.Subscription(
        topic = subscriptionTopic!!.value,
        account = account.value,
        relay = relay.toClient(),
        metadata = metadata.toClient(),
        scope = scope.toClient(),
        expiry = expiry.seconds,
    )
}

@JvmSynthetic
internal fun Push.Model.Message.toEngineDO(): EngineDO.PushMessage = EngineDO.PushMessage(title, body, icon, url, type)

@JvmSynthetic
internal fun RelayProtocolOptions.toClient(): Push.Model.Subscription.Relay {
    return Push.Model.Subscription.Relay(protocol, data)
}

@JvmSynthetic
internal fun Map<String, EngineDO.PushScope.Cached>.toClient(): Map<Push.Model.Subscription.ScopeName, Push.Model.Subscription.ScopeSetting> {
    return map { (key, value) ->
        Push.Model.Subscription.ScopeName(key) to Push.Model.Subscription.ScopeSetting(value.description, value.isSelected)
    }.toMap()
}

@JvmSynthetic
internal fun SDKError.toClient(): Push.Model.Error {
    return Push.Model.Error(exception)
}

@JvmSynthetic
internal fun Map<String, EngineDO.PushScope.Cached>.toDb(): Map<String, Pair<String, Boolean>> =
    mapValues { (_, value) -> Pair(value.description, true) }