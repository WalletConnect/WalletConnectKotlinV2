@file:JvmSynthetic

package com.walletconnect.notify.data.jwt.subscription

import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.util.jwt.encodeDidPkh
import com.walletconnect.foundation.util.jwt.encodeEd25519DidKey

internal class EncodeSubscriptionRequestJwtUseCase(
    private val dappUrl: String,
    private val accountId: AccountId,
    private val authenticationKey: PublicKey,
    private val scope: String
) : EncodeDidJwtPayloadUseCase<SubscriptionRequestJwtClaim> {

    override fun invoke(params: EncodeDidJwtPayloadUseCase.Params): SubscriptionRequestJwtClaim = with(params) {
        SubscriptionRequestJwtClaim(
            issuedAt = issuedAt,
            expiration = expiration,
            issuer = identityKeyDidKey,
            keyserverUrl = keyserverUrl,
            audience = encodeEd25519DidKey(authenticationKey.keyAsBytes),
            subject = encodeDidPkh(accountId.value),
            scope = scope,
            dappUrl = dappUrl
        )
    }
}