package com.walletconnect.android.internal.utils

import com.walletconnect.android.internal.common.model.Expiry
import java.util.*
import java.util.concurrent.TimeUnit

object CoreValidator {

    /* For account id validation reference check CAIP-10: https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-10.md#syntax */
    @JvmSynthetic
    fun isAccountIdCAIP10Compliant(accountId: String): Boolean {
        val elements = accountId.split(":")
        if (elements.isEmpty() || elements.size != 3) return false
        val (namespace: String, reference: String, accountAddress: String) = elements

        return isNamespaceValid(namespace) &&
                REFERENCE_REGEX.toRegex().matches(reference) &&
                ACCOUNT_ADDRESS_REGEX.toRegex().matches(accountAddress)
    }

    /* For chain id validation reference check CAIP-2: https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-2.md#syntax */
    @JvmSynthetic
    fun isChainIdCAIP2Compliant(chainId: String): Boolean {
        val elements: List<String> = chainId.split(":")
        if (elements.isEmpty() || elements.size != 2) return false
        val (namespace: String, reference: String) = elements
        return isNamespaceValid(namespace) && REFERENCE_REGEX.toRegex().matches(reference)
    }

    /* For namespace key validation reference check CAIP-2: https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-2.md#syntax */
    @JvmSynthetic
    fun isNamespaceValid(key: String): Boolean {
        return NAMESPACE_REGEX.toRegex().matches(key)
    }

    @JvmSynthetic
    fun isExpiryNotWithinBounds(userExpiry: Expiry?, now: Long = TimeUnit.SECONDS.convert(Date().time, TimeUnit.MILLISECONDS)): Boolean =
        userExpiry?.seconds?.run {
            val remainder = this - now

            remainder < 0 || !(FIVE_MINUTES_IN_SECONDS..WEEK_IN_SECONDS).contains(remainder)
        } ?: false

    private const val NAMESPACE_REGEX: String = "^[-a-z0-9]{3,8}$"
    private const val REFERENCE_REGEX: String = "^[-a-zA-Z0-9]{1,32}$"
    private const val ACCOUNT_ADDRESS_REGEX: String = "^[-.%a-zA-Z0-9]{1,128}$"
}