@file:JvmSynthetic

package com.walletconnect.chat.jwt.use_case

import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.chat.common.model.AccountId
import com.walletconnect.chat.jwt.ChatDidJwtClaims
import com.walletconnect.foundation.util.jwt.encodeDidPkh

internal class EncodeChatReceiptDidJwtPayloadUseCase(
    private val message: String,
    private val senderAccountId: AccountId,
) : EncodeDidJwtPayloadUseCase {

    override operator fun invoke(issuer: String, keyserverUrl: String, issuedAt: Long, expiration: Long): ChatDidJwtClaims = ChatDidJwtClaims.ChatReceipt(
        issuer = issuer,
        issuedAt = issuedAt,
        expiration = expiration,
        keyserverUrl = keyserverUrl,
        subject = sha256(message),
        audience = encodeDidPkh(senderAccountId.value),
    )
}