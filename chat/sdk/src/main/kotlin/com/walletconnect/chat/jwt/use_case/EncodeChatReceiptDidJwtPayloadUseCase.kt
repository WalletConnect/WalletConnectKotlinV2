@file:JvmSynthetic

package com.walletconnect.chat.jwt.use_case

import com.walletconnect.chat.jwt.ChatDidJwtClaims
import com.walletconnect.chat.authentication.jwt.encodeDidPkh
import com.walletconnect.chat.common.model.AccountId

internal class EncodeChatReceiptDidJwtPayloadUseCase(
    private val messageHash: String,
    private val senderAccountId: AccountId,
) : EncodeDidJwtPayloadUseCase {

    override operator fun invoke(issuer: String, keyserverUrl: String, issuedAt: Long, expiration: Long): ChatDidJwtClaims = ChatDidJwtClaims.ChatReceipt(
        issuer = issuer,
        issuedAt = issuedAt,
        expiration = expiration,
        keyserverUrl = keyserverUrl,
        subject = messageHash,
        audience = encodeDidPkh(senderAccountId),
    )
}