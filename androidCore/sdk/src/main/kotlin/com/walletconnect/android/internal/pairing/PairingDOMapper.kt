package com.walletconnect.android.internal.pairing

import com.walletconnect.android.Core

@JvmSynthetic
internal fun PairingDO.PairingDelete.toClient(): Core.Model.DeletedPairing =
    Core.Model.DeletedPairing(topic, reason)