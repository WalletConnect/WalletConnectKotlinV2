@file:JvmSynthetic

package com.walletconnect.chat.common.model

import com.walletconnect.chat.engine.domain.Validator

@JvmInline
internal value class AccountId(val value: String) {
    fun isValid(): Boolean = Validator.isAccountIdCAIP10Compliant(value)
}