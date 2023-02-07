@file:JvmSynthetic

package com.walletconnect.chat.common.model

import com.walletconnect.foundation.common.model.Topic

internal data class SendMessage(
    val topic: Topic,
    val message: ChatMessage,
    val media: Media?,
)