@file:JvmSynthetic

package com.walletconnect.notify.data.jwt.message

import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.util.jwt.encodeEd25519DidKey

internal class EncodeMessageReceiptJwtUseCase(
    private val dappUrl: String,
    private val authenticationKey: PublicKey,
    private val messageHash: String
) : EncodeDidJwtPayloadUseCase<MessageReceiptJwtClaim> {

    override fun invoke(params: EncodeDidJwtPayloadUseCase.Params): MessageReceiptJwtClaim = with(params) {
        MessageReceiptJwtClaim(
            issuedAt = issuedAt,
            expiration = expiration,
            issuer = identityKeyDidKey,
            keyserverUrl = keyserverUrl,
            audience = encodeEd25519DidKey(authenticationKey.keyAsBytes),
            subject = messageHash,
            dappUrl = dappUrl
        )
    }
}