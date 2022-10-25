package com.walletconnect.android.internal.common.model

import com.walletconnect.foundation.common.model.PublicKey

data class Participants(
    val senderPublicKey: PublicKey,
    val receiverPublicKey: PublicKey,
)