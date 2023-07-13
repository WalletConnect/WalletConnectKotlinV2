@file:JvmSynthetic

package com.walletconnect.chat.jwt.use_case

import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.chat.jwt.ChatDidJwtClaims
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.util.jwt.encodeDidPkh
import com.walletconnect.foundation.util.jwt.encodeX25519DidKey

internal class EncodeInviteApprovalDidJwtPayloadUseCase(
    private val inviteePublicKey: PublicKey,
    private val inviterAccountId: AccountId,
) : EncodeDidJwtPayloadUseCase<ChatDidJwtClaims.InviteApproval> {
    override fun invoke(params: EncodeDidJwtPayloadUseCase.Params): ChatDidJwtClaims.InviteApproval = with(params) {
        ChatDidJwtClaims.InviteApproval(
            issuer = issuer,
            issuedAt = issuedAt,
            expiration = expiration,
            keyserverUrl = keyserverUrl,
            subject = encodeX25519DidKey(inviteePublicKey.keyAsBytes),
            audience = encodeDidPkh(inviterAccountId.value),
        )
    }
}