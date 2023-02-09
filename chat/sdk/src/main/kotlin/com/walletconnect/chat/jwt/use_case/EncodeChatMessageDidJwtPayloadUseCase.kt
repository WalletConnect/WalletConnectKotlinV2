@file:JvmSynthetic

package com.walletconnect.chat.jwt.use_case

import com.walletconnect.chat.common.model.AccountId
import com.walletconnect.chat.common.model.Media
import com.walletconnect.chat.jwt.ChatDidJwtClaims
import com.walletconnect.foundation.util.jwt.encodeDidPkh

internal class EncodeChatMessageDidJwtPayloadUseCase(
    private val message: String,
    private val recipientAccountId: AccountId,
    private val media: Media?,
) : EncodeDidJwtPayloadUseCase {

    override operator fun invoke(issuer: String, keyserverUrl: String, issuedAt: Long, expiration: Long): ChatDidJwtClaims = ChatDidJwtClaims.ChatMessage(
        issuer = issuer,
        issuedAt = issuedAt,
        expiration = expiration,
        keyserverUrl = keyserverUrl,
        subject = message,
        audience = encodeDidPkh(recipientAccountId.value),
        media = media
    )
}