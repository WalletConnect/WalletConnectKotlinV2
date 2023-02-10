@file:JvmSynthetic

package com.walletconnect.chat.common.model

import com.walletconnect.chat.engine.domain.ChatValidator


@JvmInline
internal value class ChatMessage(val value: String) {
    fun isValid(): Boolean = ChatValidator.isChatMessageValid(value)
}