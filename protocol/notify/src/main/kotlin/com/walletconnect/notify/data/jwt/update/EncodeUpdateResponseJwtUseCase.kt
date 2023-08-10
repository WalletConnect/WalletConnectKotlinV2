package com.walletconnect.notify.data.jwt.update

import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.foundation.util.jwt.encodeDidPkh

class EncodeUpdateResponseJwtUseCase(
    private val dappUrl: String,
    private val accountId: AccountId
) : EncodeDidJwtPayloadUseCase<UpdateResponseJwtClaim> {

    override fun invoke(params: EncodeDidJwtPayloadUseCase.Params): UpdateResponseJwtClaim = with(params) {
        UpdateResponseJwtClaim(
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