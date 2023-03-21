@file:JvmSynthetic

package com.walletconnect.chat.jwt.use_case

import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.jwt.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.chat.jwt.ChatDidJwtClaims
import com.walletconnect.foundation.util.jwt.encodeDidPkh

internal class EncodeChatReceiptDidJwtPayloadUseCase(
    private val message: String,
    private val senderAccountId: AccountId,
) : EncodeDidJwtPayloadUseCase<ChatDidJwtClaims.ChatReceipt> {
    override fun invoke(params: EncodeDidJwtPayloadUseCase.Params): ChatDidJwtClaims.ChatReceipt = with(params) {
        ChatDidJwtClaims.ChatReceipt(
            issuer = issuer,
            issuedAt = issuedAt,
            expiration = expiration,
            keyserverUrl = keyserverUrl,
            subject = sha256(message.toByteArray()),
            audience = encodeDidPkh(senderAccountId.value),
        )
    }
}