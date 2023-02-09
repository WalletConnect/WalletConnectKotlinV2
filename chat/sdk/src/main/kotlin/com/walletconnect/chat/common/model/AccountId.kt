@file:JvmSynthetic

package com.walletconnect.chat.common.model

import com.walletconnect.android.internal.utils.CoreValidator


@JvmInline
internal value class AccountId(val value: String) {
    fun isValid(): Boolean = CoreValidator.isAccountIdCAIP10Compliant(value)
}