@file:JvmSynthetic

package com.walletconnect.notify.data.jwt

import com.walletconnect.foundation.util.jwt.JwtClaims

internal interface NotifyJwtBase : JwtClaims {
    val action: String
    val issuedAt: Long
    val expiration: Long
    val requiredActionValue: String

    private fun throwIdIssuedAtIsInvalid() {
        if (issuedAt < System.currentTimeMillis()) throw IllegalArgumentException("Invalid issuedAt claim was $issuedAt instead of lower than ${System.currentTimeMillis()}")
    }

    private fun throwExpirationAtIsInvalid() {
        if (expiration > System.currentTimeMillis()) throw IllegalArgumentException("Invalid expiration claim was $expiration instead of greater than ${System.currentTimeMillis()}")
    }

    private fun throwIfActionIsInvalid() {
        if (action != requiredActionValue) throw IllegalArgumentException("Invalid action claim was $action instead of $requiredActionValue")
    }

    fun throwIfBaseIsInvalid() {
        throwIdIssuedAtIsInvalid()
        throwExpirationAtIsInvalid()
        throwIfActionIsInvalid()
    }
}