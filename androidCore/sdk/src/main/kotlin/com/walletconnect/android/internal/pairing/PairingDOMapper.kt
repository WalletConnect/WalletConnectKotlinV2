package com.walletconnect.android.internal.pairing

import com.walletconnect.android.Core
import com.walletconnect.android.internal.PairingParams
import com.walletconnect.android.internal.pairing.PeerError.Reason.UserDisconnected.message
import com.walletconnect.foundation.common.model.Topic

@JvmSynthetic
internal fun PairingParams.DeleteParams.toEngineDO(topic: Topic): PairingDO.PairingDelete =
    PairingDO.PairingDelete(topic.value, message)

@JvmSynthetic
internal fun PairingDO.PairingDelete.toClient(topic: Topic): Core.Model.DeletedPairing =
    Core.Model.DeletedPairing(topic.value, message)