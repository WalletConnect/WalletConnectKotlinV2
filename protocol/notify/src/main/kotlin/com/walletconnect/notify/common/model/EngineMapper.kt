@file:JvmSynthetic

package com.walletconnect.notify.common.model

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.utils.toClient
import com.walletconnect.notify.client.Notify

@JvmSynthetic
internal fun Core.Model.Message.Notify.toClient(topic: String): Notify.Model.Notification.Decrypted {
    return Notify.Model.Notification.Decrypted(title, body, icon, url, type, topic)
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
internal fun NotificationType.toClient(): Notify.Model.NotificationType {
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
internal fun Notify.Model.Cacao.Signature.toCommon(): Cacao.Signature = Cacao.Signature(t, s, m)


@JvmSynthetic
internal fun DeleteSubscription.toClient(): Notify.Result.DeleteSubscription = when (this) {
    is DeleteSubscription.Success -> Notify.Result.DeleteSubscription.Success(topic)
    is DeleteSubscription.Error -> Notify.Result.DeleteSubscription.Error(Notify.Model.Error(throwable))
    DeleteSubscription.Processing -> Notify.Result.DeleteSubscription.Error(Notify.Model.Error(IllegalStateException()))
}

@JvmSynthetic
internal fun SubscriptionChanged.toClient(): Notify.Event.SubscriptionsChanged =
    Notify.Event.SubscriptionsChanged(subscriptions.map { it.toClient() })

@JvmSynthetic
internal fun CreateSubscription.toClient(): Notify.Result.Subscribe = when (this) {
    is CreateSubscription.Success -> Notify.Result.Subscribe.Success(subscription.toClient())
    is CreateSubscription.Error -> Notify.Result.Subscribe.Error(Notify.Model.Error(throwable))
    CreateSubscription.Processing -> Notify.Result.Subscribe.Error(Notify.Model.Error(IllegalStateException()))
}

@JvmSynthetic
internal fun Subscription.Active.toClient(): Notify.Model.Subscription {
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
internal fun UpdateSubscription.toClient(): Notify.Result.UpdateSubscription = when (this) {
    is UpdateSubscription.Success -> Notify.Result.UpdateSubscription.Success(subscription.toClient())
    is UpdateSubscription.Error -> Notify.Result.UpdateSubscription.Error(Notify.Model.Error(IllegalStateException()))
    UpdateSubscription.Processing -> Notify.Result.UpdateSubscription.Error(Notify.Model.Error(IllegalStateException()))

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