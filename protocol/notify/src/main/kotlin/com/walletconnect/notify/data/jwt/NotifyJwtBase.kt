@file:JvmSynthetic

package com.walletconnect.notify.data.jwt

import com.walletconnect.foundation.util.jwt.JwtClaims

internal interface NotifyJwtBase : JwtClaims {
    val action: String
    val issuedAt: Long
    val expiration: Long

    fun throwIdIssuedAtIsInvalid() {
        if (issuedAt < System.currentTimeMillis()) throw IllegalArgumentException("Invalid issuedAt claim was $issuedAt instead of lower than ${System.currentTimeMillis()}")
    }

    fun throwExpirationAtIsInvalid() {
        if (expiration > System.currentTimeMillis()) throw IllegalArgumentException("Invalid expiration claim was $expiration instead of greater than ${System.currentTimeMillis()}")
    }
}