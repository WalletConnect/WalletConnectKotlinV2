@file:JvmSynthetic

package com.walletconnect.chat.core.model.vo

import com.walletconnect.chat.engine.domain.Validator
import com.walletconnect.foundation.common.model.PublicKey

@JvmInline
internal value class AccountIdVO(val value: String) {
    fun isValid(): Boolean = Validator.isAccountIdCAIP10Compliant(value)
}

internal data class AccountIdWithPublicKeyVO(
    val accountId: AccountIdVO,
    val publicKey: PublicKey,
)

internal data class InviteVO(
    val account: String,
    val message: String,
    val signature: String? = null
)

internal data class MediaVO(
    val type: String,
    val data: String,
)

internal data class ThreadVO(
    val topic: String,
    val selfAccount: String,
    val peerAccount: String,
)

internal data class MessageVO(
    val message: String,
    val authorAccount: String,
    val timestamp: Long,
    val media: MediaVO
)