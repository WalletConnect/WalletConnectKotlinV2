@file:JvmSynthetic

package com.walletconnect.android.internal.common.model

import com.walletconnect.android.internal.utils.CoreValidator


@JvmInline
value class AccountId(val value: String) {
    fun isValid(): Boolean = CoreValidator.isAccountIdCAIP10Compliant(value)
    fun address() = value.split(":").last()
}