@file:JvmSynthetic

package com.walletconnect.notify.data.jwt

import com.walletconnect.foundation.util.jwt.JwtClaims

internal interface NotifyJwtBase: JwtClaims {
    val action: String
    val issuedAt: Long
    val expiration: Long
}