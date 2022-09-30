@file:JvmSynthetic

package com.walletconnect.auth.common.model

import com.walletconnect.android.common.model.RelayProtocolOptions
import com.walletconnect.android.common.model.SymmetricKey
import com.walletconnect.foundation.common.model.Topic

internal class WalletConnectUri(
    val topic: Topic,
    val symKey: SymmetricKey,
    val relay: RelayProtocolOptions,
    val version: String = "2",
)