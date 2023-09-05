@file:JvmSynthetic

package com.walletconnect.notify.engine.sync.model

import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.Redirect
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.notify.common.model.NotificationScope
import com.walletconnect.notify.common.model.Subscription

@JsonClass(generateAdapter = true)
internal data class SyncedSubscription(
    val topic: String,
    val account: String,
    val relay: SyncedRelayProtocolOptions,
    val metadata: SyncedMetadata,
    val scope: Map<String, SyncedScopeSetting>,
    val expiry: Long,
    val symKey: String,
)

@JsonClass(generateAdapter = true)
internal data class SyncedRelayProtocolOptions(
    val protocol: String,
    val data: String?,
)

@JsonClass(generateAdapter = true)
internal data class SyncedScopeSetting(
    val description: String,
    val enabled: Boolean,
)

@JsonClass(generateAdapter = true)
internal data class SyncedMetadata(
    val name: String,
    val description: String,
    val url: String,
    val icons: List<String>,
    val redirect: SyncedRedirect?,
    val verifyUrl: String? = null,
)

@JsonClass(generateAdapter = true)
internal data class SyncedRedirect(
    val native: String? = null,
    val universal: String? = null,
)

@JvmSynthetic
internal fun AppMetaData.toSync() = SyncedMetadata(
    name = name,
    description = description,
    url = url,
    icons = icons,
    redirect = redirect?.toSync(),
    verifyUrl = null //todo: Missing in Metadata in Core data structure
)

@JvmSynthetic
internal fun RelayProtocolOptions.toSync() = SyncedRelayProtocolOptions(
    protocol = protocol,
    data = data
)

@JvmSynthetic
internal fun Redirect.toSync() = SyncedRedirect(
    native = native,
    universal = universal
)

@JvmSynthetic
internal fun Map<String, NotificationScope.Cached>.toSync(): Map<String, SyncedScopeSetting> = mapValues {
    SyncedScopeSetting(
        description = it.value.description,
        enabled = it.value.isSelected
    )
}

@JvmSynthetic
internal fun Subscription.Active.toSync(symmetricKey: SymmetricKey) = SyncedSubscription(
    topic = notifyTopic.value,
    account = account.value,
    relay = relay.toSync(),
    metadata = dappMetaData!!.toSync(),
    scope = mapOfNotificationScope.toSync(),
    expiry = expiry.seconds,
    symKey = symmetricKey.keyAsHex
)

@JvmSynthetic
internal fun SyncedSubscription.toCommon(authenticationPublicKey: PublicKey): Subscription.Active = Subscription.Active(
    account = AccountId(account),
    authenticationPublicKey = authenticationPublicKey,
    mapOfNotificationScope = scope.toCommon(),
    expiry = Expiry(expiry),
    dappGeneratedPublicKey = PublicKey("There is no dapp generated public key from sync"),
    notifyTopic = Topic(topic),
    relay = RelayProtocolOptions(relay.protocol, relay.data),
    dappMetaData = metadata.toCommon()
)

@JvmSynthetic
internal fun Map<String, SyncedScopeSetting>.toCommon(): Map<String, NotificationScope.Cached> = mapValues {
    NotificationScope.Cached(
        name = it.key,
        description = it.value.description,
        isSelected = it.value.enabled
    )
}

@JvmSynthetic
internal fun SyncedMetadata.toCommon() = AppMetaData(
    name = name,
    description = description,
    url = url,
    icons = icons,
    redirect = redirect?.toCommon(),
//    verifyUrl = null //todo: Missing in Metadata in Core data structure
)

@JvmSynthetic
internal fun SyncedRedirect.toCommon() = Redirect(
    native = native,
    universal = universal
)