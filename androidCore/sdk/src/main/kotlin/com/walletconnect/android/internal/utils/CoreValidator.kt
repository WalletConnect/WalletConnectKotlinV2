package com.walletconnect.android.internal.utils

import com.walletconnect.android.internal.common.model.Expiry

object CoreValidator {

    /* For account id validation reference check CAIP-10: https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-10.md#syntax */
    @JvmSynthetic
    fun isAccountIdCAIP10Compliant(accountId: String): Boolean {
        val elements = accountId.split(":")
        if (elements.isEmpty() || elements.size != 3) return false
        val (namespace: String, reference: String, accountAddress: String) = elements

        return isNamespaceRegexCompliant(namespace) &&
                REFERENCE_REGEX.toRegex().matches(reference) &&
                ACCOUNT_ADDRESS_REGEX.toRegex().matches(accountAddress)
    }

    /* For chain id validation reference check CAIP-2: https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-2.md#syntax */
    @JvmSynthetic
    fun isChainIdCAIP2Compliant(chainId: String): Boolean {
        val elements: List<String> = chainId.split(":")
        if (elements.isEmpty() || elements.size != 2) return false
        val (namespace: String, reference: String) = elements
        return isNamespaceRegexCompliant(namespace) && REFERENCE_REGEX.toRegex().matches(reference)
    }

    /* For namespace key validation reference check CAIP-2: https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-2.md#syntax */
    @JvmSynthetic
    fun isNamespaceRegexCompliant(key: String): Boolean {
        return NAMESPACE_REGEX.toRegex().matches(key)
    }

    @JvmSynthetic
    fun isExpiryWithinBounds(userExpiry: Expiry?): Boolean =
        userExpiry?.seconds?.run {
            (FIVE_MINUTES_IN_SECONDS..WEEK_IN_SECONDS).contains(this)
        } ?: true

    private const val NAMESPACE_REGEX: String = "^[-a-z0-9]{3,8}$"
    private const val REFERENCE_REGEX: String = "^[-_a-zA-Z0-9]{1,32}$"
    private const val ACCOUNT_ADDRESS_REGEX: String = "^[-.%a-zA-Z0-9]{1,128}$"
}