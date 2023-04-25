package com.walletconnect.android.pairing.model.mapper

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.Pairing
import com.walletconnect.android.internal.common.model.Redirect
import com.walletconnect.android.pairing.engine.model.EngineDO
import com.walletconnect.foundation.common.model.Topic

@JvmSynthetic
internal fun EngineDO.PairingDelete.toClient(): Core.Model.DeletedPairing =
    Core.Model.DeletedPairing(topic, reason)

@JvmSynthetic
internal fun Pairing.toClient(): Core.Model.Pairing =
    Core.Model.Pairing(
        topic.value,
        expiry.seconds,
        peerAppMetaData?.toClient(),
        relayProtocol,
        relayData,
        uri,
        isActive,
        registeredMethods
    )

@JvmSynthetic
fun Core.Model.Pairing.toPairing(): Pairing =
    Pairing(
        Topic(topic),
        Expiry(expiry),
        peerAppMetaData?.toAppMetaData(),
        relayProtocol,
        relayData,
        uri,
        isActive,
        registeredMethods
    )

@JvmSynthetic
internal fun Core.Model.AppMetaData.toAppMetaData() = AppMetaData(name = name, description = description, url =  url, icons = icons, redirect = Redirect(redirect))

fun AppMetaData.toClient() = Core.Model.AppMetaData(name, description, url, icons, redirect?.native)