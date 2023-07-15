@file:JvmSynthetic

package com.walletconnect.push.common.data.jwt

import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.foundation.util.jwt.encodeDidPkh

class EncodePushAuthDidJwtPayloadUseCase(
    private val audience: String,
    private val accountId: AccountId,
    private val scope: String
) : EncodeDidJwtPayloadUseCase<PushSubscriptionJwtClaim> {

    override fun invoke(params: EncodeDidJwtPayloadUseCase.Params): PushSubscriptionJwtClaim = with(params) {
        PushSubscriptionJwtClaim(
            issuedAt = issuedAt,
            expiration = expiration,
            issuer = issuer,
            keyserverUrl = keyserverUrl,
            audience = audience,
            subject = encodeDidPkh(accountId.value),
            scope = scope
        )
    }
}