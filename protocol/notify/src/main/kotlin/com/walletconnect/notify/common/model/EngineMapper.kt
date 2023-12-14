@file:JvmSynthetic

package com.walletconnect.notify.common.model

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.utils.toClient
import com.walletconnect.notify.client.Notify

@JvmSynthetic
internal fun Core.Model.Message.Notify.toWalletClient(topic: String): Notify.Model.Message.Decrypted {
    return Notify.Model.Message.Decrypted(title, body, icon, url, type, topic)
}


@JvmSynthetic
internal fun Core.Model.Message.Notify.toClient(topic: String): Notify.Model.Notification.Decrypted {
    return Notify.Model.Notification.Decrypted(title, body, icon, url, type, topic)
}


@JvmSynthetic
internal fun NotifyMessage.toClient(topic: String): Notify.Model.Notification.Decrypted {
    return Notify.Model.Notification.Decrypted(title, body, icon, url, type, topic)
}

@JvmSynthetic
internal fun NotifyRecord.toWalletClient(): Notify.Model.MessageRecord {
    return Notify.Model.MessageRecord(
        id = this.id.toString(),
        topic = this.topic,
        publishedAt = this.publishedAt,
        message = Notify.Model.Message.Decrypted(
            title = this.notifyMessage.title,
            body = this.notifyMessage.body,
            icon = this.notifyMessage.icon,
            url = this.notifyMessage.url,
            type = this.notifyMessage.type,
            topic = this.topic
        )
    )
}


@JvmSynthetic
@Throws(IllegalArgumentException::class)
internal fun NotifyRecord.toClient(): Notify.Model.NotificationRecord {
    return Notify.Model.NotificationRecord(
        id = this.id.toString(),
        topic = this.topic,
        publishedAt = this.publishedAt,
        message = Notify.Model.Notification.Decrypted(
            title = this.notifyMessage.title,
            body = this.notifyMessage.body,
            icon = this.notifyMessage.icon,
            url = this.notifyMessage.url,
            type = this.notifyMessage.type,
            topic = this.topic
        ),
        metadata = this.metadata?.let { it.toClient() } ?: run { throw IllegalArgumentException("Metadata is null") }
    )
}


@JvmSynthetic
internal fun NotificationType.toWalletClient(): Notify.Model.NotificationType {
    return Notify.Model.NotificationType(id, name, description)
}


@JvmSynthetic
internal fun CacaoPayloadWithIdentityPrivateKey.toClient(): Notify.Model.CacaoPayloadWithIdentityPrivateKey {
    return Notify.Model.CacaoPayloadWithIdentityPrivateKey(payload.toClient(), key)
}


@JvmSynthetic
internal fun Notify.Model.CacaoPayloadWithIdentityPrivateKey.toCommon(): CacaoPayloadWithIdentityPrivateKey {
    return CacaoPayloadWithIdentityPrivateKey(payload.toCommon(), key)
}


@JvmSynthetic
internal fun Cacao.Payload.toClient(): Notify.Model.Cacao.Payload {
    return Notify.Model.Cacao.Payload(iss, domain, aud, version, nonce, iat, nbf, exp, statement, requestId, resources)
}


@JvmSynthetic
internal fun Notify.Model.Cacao.Payload.toCommon(): Cacao.Payload {
    return Cacao.Payload(iss, domain, aud, version, nonce, iat, nbf, exp, statement, requestId, resources)
}

@JvmSynthetic
internal fun ((String) -> Notify.Model.Cacao.Signature?).toWalletClient(): (String) -> Cacao.Signature? = { message ->
    this(message)?.let { publicCacaoSignature: Notify.Model.Cacao.Signature ->
        Cacao.Signature(publicCacaoSignature.t, publicCacaoSignature.s, publicCacaoSignature.m)
    }
}


@JvmSynthetic
internal fun Notify.Model.Cacao.Signature.toCommon(): Cacao.Signature = Cacao.Signature(t, s, m)


@JvmSynthetic
internal fun DeleteSubscription.toWalletClient(): Notify.Event.Delete {
    return Notify.Event.Delete(topic)
}

@JvmSynthetic
internal fun SubscriptionChanged.toWalletClient(): Notify.Event.SubscriptionsChanged =
    Notify.Event.SubscriptionsChanged(subscriptions.map { it.toWalletClient() })


@JvmSynthetic
internal fun Subscription.Active.toWalletClient(): Notify.Model.Subscription =
    Notify.Model.Subscription(
        topic = notifyTopic.value,
        account = account.value,
        relay = relay.toClient(),
        metadata = dappMetaData.toClient(),
        scope = mapOfNotificationScope.toClient(),
        expiry = expiry.seconds,
    )


@JvmSynthetic
internal fun Subscription.Active.toEvent(): Notify.Event.Subscription.Result =
    Notify.Event.Subscription.Result(toWalletClient())


@JvmSynthetic
internal fun Subscription.Active.toModel(): Notify.Model.Subscription {
    return Notify.Model.Subscription(
        topic = notifyTopic.value,
        account = account.value,
        relay = relay.toClient(),
        metadata = dappMetaData.toClient(),
        scope = mapOfNotificationScope.toClient(),
        expiry = expiry.seconds,
    )
}

@JvmSynthetic
internal fun Error.toWalletClient(): Notify.Event.Subscription.Error {
    return Notify.Event.Subscription.Error(requestId, rejectionReason)
}

@JvmSynthetic
internal fun UpdateSubscription.Result.toWalletClient(): Notify.Event.Update.Result {
    return Notify.Event.Update.Result(
        Notify.Model.Subscription(
            topic = notifyTopic.value,
            account = account.value,
            relay = relay.toClient(),
            metadata = dappMetaData.toClient(),
            scope = mapOfNotificationScope.toClient(),
            expiry = expiry.seconds,
        )
    )
}

@JvmSynthetic
internal fun UpdateSubscription.Error.toWalletClient(): Notify.Event.Update.Error {
    return Notify.Event.Update.Error(requestId, rejectionReason)
}

@JvmSynthetic
internal fun RelayProtocolOptions.toClient(): Notify.Model.Subscription.Relay {
    return Notify.Model.Subscription.Relay(protocol, data)
}

@JvmSynthetic
internal fun Map<String, NotificationScope.Cached>.toClient(): Map<Notify.Model.Subscription.ScopeId, Notify.Model.Subscription.ScopeSetting> {
    return map { (key, value) ->
        Notify.Model.Subscription.ScopeId(key) to Notify.Model.Subscription.ScopeSetting(value.name, value.description, value.isSelected)
    }.toMap()
}

@JvmSynthetic
internal fun SDKError.toClient(): Notify.Model.Error {
    return Notify.Model.Error(exception)
}

@JvmSynthetic
internal fun Map<String, NotificationScope.Cached>.toDb(): Map<String, Triple<String, String, Boolean>> {
    return mapValues { (_, value) -> Triple(value.name, value.description, value.isSelected) }
}