@file:JvmSynthetic

package com.walletconnect.chat.jwt.use_case

import com.walletconnect.android.internal.common.jwt.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.chat.common.model.Media
import com.walletconnect.chat.jwt.ChatDidJwtClaims
import com.walletconnect.foundation.util.jwt.encodeDidPkh
import com.walletconnect.foundation.util.jwt.jwtIatAndExp
import java.util.concurrent.TimeUnit

internal class EncodeChatMessageDidJwtPayloadUseCase(
    private val message: String,
    private val recipientAccountId: AccountId,
    private val media: Media?,
    private val timestampInMs: Long,
) : EncodeDidJwtPayloadUseCase<ChatDidJwtClaims.ChatMessage> {
    override fun invoke(params: EncodeDidJwtPayloadUseCase.Params): ChatDidJwtClaims.ChatMessage = with(params) {
        val (messageIssuedAt, messageExpiration) = jwtIatAndExp(timestampInMs = timestampInMs, timeunit = TimeUnit.MILLISECONDS, expirySourceDuration = 30, expiryTimeUnit = TimeUnit.DAYS)

        return ChatDidJwtClaims.ChatMessage(
            issuer = issuer,
            issuedAt = messageIssuedAt,
            expiration = messageExpiration,
            keyserverUrl = keyserverUrl,
            subject = message,
            audience = encodeDidPkh(recipientAccountId.value),
            media = media
        )
    }
}