@file:JvmSynthetic

package com.walletconnect.chat.copiedFromSign.core.model.vo.sync

import com.walletconnect.chat.copiedFromSign.core.model.vo.PublicKey

internal data class ParticipantsVO(
    val senderPublicKey: PublicKey,
    val receiverPublicKey: PublicKey,
)