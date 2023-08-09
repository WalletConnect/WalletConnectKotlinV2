@file:JvmSynthetic

package com.walletconnect.notify.data.jwt

import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.foundation.util.jwt.encodeDidPkh

class EncodeNotifyAuthDidJwtPayloadUseCase(
    private val audience: String,
    private val accountId: AccountId,
    private val scope: String
) : EncodeDidJwtPayloadUseCase<NotifySubscriptionJwtClaim> {

    override fun invoke(params: EncodeDidJwtPayloadUseCase.Params): NotifySubscriptionJwtClaim = with(params) {
        NotifySubscriptionJwtClaim(
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