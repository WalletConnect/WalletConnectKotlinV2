package com.walletconnect.push.wallet.engine.sync.model

import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.Redirect
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.push.common.model.EngineDO


@JsonClass(generateAdapter = true)
internal data class SyncedSubscription(
    val topic: String,
    val account: String,
    val relay: SyncedRelayProtocolOptions,
    val metadata: SyncedMetadata?,
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
    val name: String,
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

internal fun AppMetaData.toSync() = SyncedMetadata(
    name = name,
    description = description,
    url = url,
    icons = icons,
    redirect = redirect?.toSync(),
    verifyUrl = null //todo: Missing in Metadata in Core data structure
)


internal fun RelayProtocolOptions.toSync() = SyncedRelayProtocolOptions(
    protocol = protocol,
    data = data
)

internal fun Redirect.toSync() = SyncedRedirect(
    native = native,
    universal = universal
)

internal fun Map<String, EngineDO.PushScope.Cached>.toSync(): Map<String, SyncedScopeSetting> = mapValues {
    SyncedScopeSetting(
        name = it.value.name,
        description = it.value.description,
        enabled = it.value.isSelected
    )
}

internal fun EngineDO.Subscription.Active.toSync(symmetricKey: SymmetricKey) = SyncedSubscription(
    topic = pushTopic.value,
    account = account.value,
    relay = relay.toSync(),
    metadata = dappMetaData?.toSync(),
    scope = mapOfScope.toSync(),
    expiry = expiry.seconds,
    symKey = symmetricKey.keyAsHex
)

internal fun SyncedSubscription.toCommon(): EngineDO.Subscription.Active= EngineDO.Subscription.Active(
    account = AccountId(account),
    mapOfScope = scope.toCommon(),
    expiry = Expiry(expiry),
    dappGeneratedPublicKey = PublicKey("There is no dapp generated public key from sync"),
    pushTopic = Topic(topic),
    relay = RelayProtocolOptions(relay.protocol, relay.data),
    dappMetaData = metadata?.toCommon()
)


internal fun Map<String, SyncedScopeSetting>.toCommon(): Map<String, EngineDO.PushScope.Cached> = mapValues {
    EngineDO.PushScope.Cached(
        name = it.value.name,
        description = it.value.description,
        isSelected = it.value.enabled
    )
}


internal fun SyncedMetadata.toCommon() = AppMetaData(
    name = name,
    description = description,
    url = url,
    icons = icons,
    redirect = redirect?.toCommon(),
//    verifyUrl = null //todo: Missing in Metadata in Core data structure
)

internal fun SyncedRedirect.toCommon() = Redirect(
    native = native,
    universal = universal
)