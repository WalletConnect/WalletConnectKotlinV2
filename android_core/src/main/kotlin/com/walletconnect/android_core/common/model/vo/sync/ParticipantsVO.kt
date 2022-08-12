@file:JvmSynthetic

package com.walletconnect.android_core.common.model.vo.sync

import com.walletconnect.foundation.common.model.PublicKey

internal data class ParticipantsVO(
    val senderPublicKey: PublicKey,
    val receiverPublicKey: PublicKey,
)