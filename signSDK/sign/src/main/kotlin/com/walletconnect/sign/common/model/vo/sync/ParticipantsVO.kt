@file:JvmSynthetic

package com.walletconnect.sign.common.model.vo.sync

import com.walletconnect.foundation.common.model.PublicKey

internal data class ParticipantsVO(
    val senderPublicKey: PublicKey,
    val receiverPublicKey: PublicKey,
)