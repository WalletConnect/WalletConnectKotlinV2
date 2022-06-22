@file:JvmSynthetic

package com.walletconnect.sign.core.model.vo.sync

import com.walletconnect.sign.core.model.vo.PublicKey

internal data class ParticipantsVO(
    val senderPublicKey: PublicKey,
    val receiverPublicKey: PublicKey,
)