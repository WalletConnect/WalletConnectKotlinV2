@file:JvmSynthetic

package com.walletconnect.chat.common.model

import com.walletconnect.foundation.common.model.Topic

internal data class Message(
    val topic: Topic,
    val message: ChatMessage,
    val authorAccount: AccountId,
    val timestamp: Long,
    val media: Media?,
)