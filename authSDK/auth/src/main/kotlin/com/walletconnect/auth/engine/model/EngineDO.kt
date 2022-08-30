@file:JvmSynthetic

package com.walletconnect.auth.engine.model

import com.walletconnect.android.impl.common.model.SymmetricKey
import com.walletconnect.foundation.common.model.Topic

internal sealed class EngineDO {

    internal class WalletConnectUri(
        val topic: Topic,
        val symKey: SymmetricKey,
        val relay: RelayProtocolOptions,
        val version: String = "2"
    ) : EngineDO()

    internal data class RelayProtocolOptions(val protocol: String, val data: String? = null) : EngineDO()

    internal data class AppMetaData(
        val name: String,
        val description: String,
        val url: String,
        val icons: List<String>,
        val redirect: String?,
    ) : EngineDO()
}