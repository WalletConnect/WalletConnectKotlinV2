@file:JvmSynthetic

package com.walletconnect.chat.jwt.use_case

import com.walletconnect.chat.common.model.AccountId
import com.walletconnect.chat.jwt.ChatDidJwtClaims
import com.walletconnect.foundation.util.jwt.encodeDidPkh
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.hexToBytes
import java.security.MessageDigest

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

    private fun sha256(key: String): String {
        val messageDigest: MessageDigest = MessageDigest.getInstance("SHA-256")
        val hashedBytes: ByteArray = messageDigest.digest(key.hexToBytes())
        return hashedBytes.bytesToHex()
    }
}