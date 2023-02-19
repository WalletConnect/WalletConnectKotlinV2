@file:JvmSynthetic

package com.walletconnect.chat.common.model

import com.walletconnect.chat.engine.domain.ChatValidator

@JvmInline
internal value class InviteMessage(val value: String) {
    fun isValid(): Boolean = ChatValidator.isInviteMessageValid(value)
}