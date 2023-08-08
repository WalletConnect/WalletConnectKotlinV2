@file:JvmSynthetic

package com.walletconnect.notify.data.jwt.subscription

import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.foundation.util.jwt.encodeDidPkh
import com.walletconnect.notify.data.jwt.subscription.SubscriptionRequestJwtClaim

class EncodeSubscriptionRequestJwtUseCase(
    private val dappDomain: String,
    private val accountId: AccountId,
    private val scope: String
) : EncodeDidJwtPayloadUseCase<SubscriptionRequestJwtClaim> {

    override fun invoke(params: EncodeDidJwtPayloadUseCase.Params): SubscriptionRequestJwtClaim = with(params) {
        SubscriptionRequestJwtClaim(
            issuedAt = issuedAt,
            expiration = expiration,
            issuer = identityKeyDidKey,
            keyserverUrl = keyserverUrl,
            audience = identityKeyDidKey,
            subject = encodeDidPkh(accountId.value),
            scope = scope,
            dappDomain = dappDomain
        )
    }
}