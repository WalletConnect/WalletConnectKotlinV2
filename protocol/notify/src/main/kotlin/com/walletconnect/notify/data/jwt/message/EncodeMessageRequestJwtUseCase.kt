package com.walletconnect.notify.data.jwt.message

import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.util.jwt.encodeDidPkh
import com.walletconnect.foundation.util.jwt.encodeEd25519DidKey
import com.walletconnect.notify.data.jwt.delete.DeleteRequestJwtClaim

class EncodeMessageRequestJwtUseCase(
    private val accountId: AccountId,
    private val dappUrl: String,
    private val authenticationKey: PublicKey,
) : EncodeDidJwtPayloadUseCase<MessageRequestJwtClaim> {

    override fun invoke(params: EncodeDidJwtPayloadUseCase.Params): MessageRequestJwtClaim = with(params) {
        MessageRequestJwtClaim(
            issuedAt = issuedAt,
            expiration = expiration,
            issuer = identityKeyDidKey,
            keyserverUrl = keyserverUrl,
            audience = encodeEd25519DidKey(authenticationKey.keyAsBytes),
            subject = encodeDidPkh(accountId.value),
            dappUrl = dappUrl,
            message = MessageRequestJwtClaim.Message("", "", "", "", "")
        )
    }
}