@file:JvmSynthetic

package com.walletconnect.notify.data.jwt.update

import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.util.jwt.encodeDidPkh
import com.walletconnect.foundation.util.jwt.encodeEd25519DidKey

internal class EncodeUpdateRequestJwtUseCase(
    private val accountId: AccountId,
    private val dappUrl: String,
    private val authenticationKey: PublicKey,
    private val scope: String
) : EncodeDidJwtPayloadUseCase<UpdateRequestJwtClaim> {

    override fun invoke(params: EncodeDidJwtPayloadUseCase.Params): UpdateRequestJwtClaim = with(params) {
        UpdateRequestJwtClaim(
            issuedAt = issuedAt,
            expiration = expiration,
            issuer = issuer,
            keyserverUrl = keyserverUrl,
            audience = encodeEd25519DidKey(authenticationKey.keyAsBytes),
            subject = encodeDidPkh(accountId.value),
            dappUrl = dappUrl,
            scope = scope,
        )
    }
}