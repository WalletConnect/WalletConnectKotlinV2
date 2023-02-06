@file:JvmSynthetic

package com.walletconnect.chat.jwt.use_case

import com.walletconnect.chat.common.model.AccountId
import com.walletconnect.chat.jwt.ChatDidJwtClaims
import com.walletconnect.foundation.util.jwt.encodeDidPkh

internal class EncodeInviteKeyDidJwtPayloadUseCase(
    private val invitePublicKey: String,
    private val accountId: AccountId,
) : EncodeDidJwtPayloadUseCase {

    override operator fun invoke(issuer: String, keyserverUrl: String, issuedAt: Long, expiration: Long): ChatDidJwtClaims = ChatDidJwtClaims.InviteKey(
        issuer = issuer,
        issuedAt = issuedAt,
        expiration = expiration,
        audience = keyserverUrl,
        subject = invitePublicKey,
        pkh = encodeDidPkh(accountId.value)
    )
}