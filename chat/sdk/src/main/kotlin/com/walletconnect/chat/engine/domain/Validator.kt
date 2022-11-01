package com.walletconnect.chat.engine.domain

// TODO: This was copied from Sign SDK. Maybe we should move it to core?
internal object Validator {

    @JvmSynthetic
    internal fun isAccountIdCAIP10Compliant(accountId: String): Boolean {
        val elements = accountId.split(":")
        if (elements.isEmpty() || elements.size != 3) return false
        val (namespace: String, reference: String, accountAddress: String) = elements

        return NAMESPACE_REGEX.toRegex().matches(namespace) &&
                REFERENCE_REGEX.toRegex().matches(reference) &&
                ACCOUNT_ADDRESS_REGEX.toRegex().matches(accountAddress)
    }

    private const val NAMESPACE_REGEX: String = "^[-a-z0-9]{3,8}$"
    private const val REFERENCE_REGEX: String = "^[-a-zA-Z0-9]{1,32}$"
    private const val ACCOUNT_ADDRESS_REGEX: String = "^[a-zA-Z0-9]{1,64}$"
}