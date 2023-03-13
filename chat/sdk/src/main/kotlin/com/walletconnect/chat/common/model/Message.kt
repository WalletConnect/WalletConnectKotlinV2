@file:JvmSynthetic

package com.walletconnect.chat.common.model

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.foundation.common.model.Topic

internal data class Message(
    val messageId: Long,
    val topic: Topic,
    val message: ChatMessage,
    val authorAccount: AccountId,
    val timestamp: Long, // We might need additional deliveryTimestamp for ordering
    val media: Media?,
)