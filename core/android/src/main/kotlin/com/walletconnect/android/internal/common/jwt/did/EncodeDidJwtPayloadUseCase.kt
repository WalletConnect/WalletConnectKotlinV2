@file:JvmSynthetic

package com.walletconnect.android.internal.common.jwt.did

import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.util.jwt.JwtClaims
import com.walletconnect.foundation.util.jwt.encodeEd25519DidKey
import com.walletconnect.foundation.util.jwt.jwtIatAndExp
import java.util.concurrent.TimeUnit

interface EncodeDidJwtPayloadUseCase<R : JwtClaims> {

    operator fun invoke(params: Params): R

    data class Params(val identityPublicKey: PublicKey, val keyserverUrl: String, val expirySourceDuration: Long = 30, val expiryTimeUnit: TimeUnit = TimeUnit.DAYS) {
        private val iatAndExp = jwtIatAndExp(timeunit = TimeUnit.SECONDS, expirySourceDuration = expirySourceDuration, expiryTimeUnit = expiryTimeUnit)

        val issuedAt: Long
            get() = iatAndExp.first

        val expiration: Long
            get() = iatAndExp.second

        val identityKeyDidKey: String
            get() = encodeEd25519DidKey(identityPublicKey.keyAsBytes)
    }
}

