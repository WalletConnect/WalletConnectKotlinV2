package com.walletconnect.notify.data.jwt.delete

import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.foundation.util.jwt.encodeDidPkh

class EncodeDeleteResponseJwtUseCase(
    private val dappUrl: String,
    private val accountId: AccountId
) : EncodeDidJwtPayloadUseCase<DeleteResponseJwtClaim> {

    override fun invoke(params: EncodeDidJwtPayloadUseCase.Params): DeleteResponseJwtClaim = with(params) {
        DeleteResponseJwtClaim(
            issuedAt = issuedAt,
            expiration = expiration,
            issuer = identityKeyDidKey,
            keyserverUrl = keyserverUrl,
            audience = identityKeyDidKey,
            subject = encodeDidPkh(accountId.value),
            dappUrl = dappUrl
        )
    }
}