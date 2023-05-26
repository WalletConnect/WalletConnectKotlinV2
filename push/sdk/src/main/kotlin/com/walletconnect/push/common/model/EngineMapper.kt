package com.walletconnect.push.common.model

import com.walletconnect.android.internal.common.model.params.PushParams
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.push.common.Push

@JvmSynthetic
internal fun PushParams.MessageParams.toEngineDO(): EngineDO.PushMessage =
    EngineDO.PushMessage(title, body, icon, url, type)

@JvmSynthetic
internal fun EngineDO.PushRequest.toWalletClient(): Push.Wallet.Event.Request {
    return Push.Wallet.Event.Request(id, metaData.toClient())
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
internal fun EngineDO.PushDelete.toWalletClient(): Push.Wallet.Event.Delete {
    return Push.Wallet.Event.Delete(topic)
}

@JvmSynthetic
internal fun EngineDO.PushSubscription.toWalletClient(): Push.Wallet.Event.Subscription.Result {
    return Push.Wallet.Event.Subscription.Result(
        Push.Model.Subscription(
            requestId = requestId,
            topic = subscriptionTopic!!.value,
            account = account.value,
            relay = relay.toClient(),
            metadata = metadata.toClient(),
            scope = scope.toClient(),
            expiry = expiry.seconds,
        )
    )
}

@JvmSynthetic
internal fun EngineDO.PushSubscribeError.toWalletClient(): Push.Wallet.Event.Subscription.Error {
    return Push.Wallet.Event.Subscription.Error(requestId, rejectionReason)
}

@JvmSynthetic
internal fun EngineDO.PushUpdate.toWalletClient(): Push.Wallet.Event.Update.Result {
    return Push.Wallet.Event.Update.Result(
        Push.Model.Subscription(
            requestId = requestId,
            topic = subscriptionTopic!!,
            account = account.value,
            relay = relay.toClient(),
            metadata = metadata.toClient(),
            scope = scope.toClient(),
            expiry = expiry.seconds,
        )
    )
}

@JvmSynthetic
internal fun EngineDO.PushUpdateError.toWalletClient(): Push.Wallet.Event.Update.Error {
    return Push.Wallet.Event.Update.Error(requestId, rejectionReason)
}

@JvmSynthetic
internal fun EngineDO.PushRequestResponse.toDappClient(): Push.Dapp.Event.Response {
    return Push.Dapp.Event.Response(
        Push.Model.Subscription(
            requestId = subscription.requestId,
            topic = subscription.subscriptionTopic!!.value,
            account = subscription.account.value,
            relay = subscription.relay.toClient(),
            metadata = subscription.metadata.toClient(),
            scope = subscription.scope.toClient(),
            expiry = subscription.expiry.seconds,
        )
    )
}

@JvmSynthetic
internal fun EngineDO.PushDelete.toDappClient(): Push.Dapp.Event.Delete {
    return Push.Dapp.Event.Delete(topic)
}

@JvmSynthetic
internal fun EngineDO.PushRequestRejected.toDappClient(): Push.Dapp.Event.Rejected {
    return Push.Dapp.Event.Rejected(rejectionReason)
}

@JvmSynthetic
internal fun EngineDO.PushSubscription.toDappClient(): Push.Model.Subscription {
    return Push.Model.Subscription(
        requestId = requestId,
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