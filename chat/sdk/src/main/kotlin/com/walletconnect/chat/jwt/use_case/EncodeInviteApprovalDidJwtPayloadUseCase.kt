@file:JvmSynthetic

package com.walletconnect.chat.jwt.use_case

import com.walletconnect.chat.jwt.ChatDidJwtClaims
import com.walletconnect.chat.authentication.jwt.encodeDidPkh
import com.walletconnect.chat.authentication.jwt.encodeX25519DidKey
import com.walletconnect.chat.common.model.AccountId
import com.walletconnect.foundation.common.model.PublicKey

internal class EncodeInviteApprovalDidJwtPayloadUseCase(
    private val inviteePublicKey: PublicKey,
    private val inviterAccountId: AccountId,
) : EncodeDidJwtPayloadUseCase {

    override operator fun invoke(issuer: String, keyserverUrl: String, issuedAt: Long, expiration: Long): ChatDidJwtClaims = ChatDidJwtClaims.InviteApproval(
        issuer = issuer,
        issuedAt = issuedAt,
        expiration = expiration,
        keyserverUrl = keyserverUrl,
        subject = encodeX25519DidKey(inviteePublicKey.keyAsBytes),
        audience = encodeDidPkh(inviterAccountId),
    )
}