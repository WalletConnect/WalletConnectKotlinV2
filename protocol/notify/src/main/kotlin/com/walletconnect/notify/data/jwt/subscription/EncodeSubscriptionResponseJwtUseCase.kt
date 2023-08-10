package com.walletconnect.notify.data.jwt.subscription

import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.util.jwt.encodeX25519DidKey

class EncodeSubscriptionResponseJwtUseCase(
    private val dappUrl: String,
    private val publicKey: PublicKey
): EncodeDidJwtPayloadUseCase<SubscriptionResponseJwtClaim> {

    override fun invoke(params: EncodeDidJwtPayloadUseCase.Params): SubscriptionResponseJwtClaim = with(params) {
        SubscriptionResponseJwtClaim(
            issuedAt = issuedAt,
            expiration = expiration,
            issuer = identityKeyDidKey,
            keyserverUrl = keyserverUrl,
            audience = identityKeyDidKey,
            subject = encodeX25519DidKey(publicKey.keyAsBytes),
            dappUrl = dappUrl
        )
    }
}