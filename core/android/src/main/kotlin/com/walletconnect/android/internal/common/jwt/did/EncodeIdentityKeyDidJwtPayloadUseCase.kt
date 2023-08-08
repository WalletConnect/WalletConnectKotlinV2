package com.walletconnect.android.internal.common.jwt.did

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.foundation.util.jwt.JwtClaims
import com.walletconnect.foundation.util.jwt.encodeDidPkh

internal class EncodeIdentityKeyDidJwtPayloadUseCase(
    private val accountId: AccountId,
) : EncodeDidJwtPayloadUseCase<EncodeIdentityKeyDidJwtPayloadUseCase.IdentityKey> {

    override fun invoke(params: EncodeDidJwtPayloadUseCase.Params): IdentityKey = with(params) {
        IdentityKey(
            issuer = identityKeyDidKey,
            issuedAt = issuedAt,
            expiration = expiration,
            audience = keyserverUrl,
            pkh = encodeDidPkh(accountId.value)
        )
    }

    @JsonClass(generateAdapter = true)
    data class IdentityKey(
        @Json(name = "iss") override val issuer: String,
        @Json(name = "aud") val audience: String,
        @Json(name = "iat") val issuedAt: Long,
        @Json(name = "exp") val expiration: Long,
        @Json(name = "pkh") val pkh: String,
        @Json(name = "act") val act: String = "unregister_identity",
    ) : JwtClaims
}