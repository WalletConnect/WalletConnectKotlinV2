package com.walletconnect.notify.data.jwt.delete

import com.walletconnect.android.internal.common.exception.Reason
import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.util.jwt.encodeEd25519DidKey

class EncodeDeleteRequestJwtUseCase(
    private val dappUrl: String,
    private val authenticationKey: PublicKey,
) : EncodeDidJwtPayloadUseCase<DeleteRequestJwtClaim> {

    override fun invoke(params: EncodeDidJwtPayloadUseCase.Params): DeleteRequestJwtClaim = with(params) {
        DeleteRequestJwtClaim(
            issuedAt = issuedAt,
            expiration = expiration,
            issuer = identityKeyDidKey,
            keyserverUrl = keyserverUrl,
            audience = encodeEd25519DidKey(authenticationKey.keyAsBytes),
            subject = Reason.UserDisconnected.message,
            dappUrl = dappUrl
        )
    }
}