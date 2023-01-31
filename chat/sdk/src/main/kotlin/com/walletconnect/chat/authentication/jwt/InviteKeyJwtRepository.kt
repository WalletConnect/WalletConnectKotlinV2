package com.walletconnect.chat.authentication.jwt

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.walletconnect.chat.common.model.AccountId
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import io.ipfs.multibase.Base58
import io.ipfs.multibase.Multibase
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.util.*
import java.util.concurrent.TimeUnit

// TODO: Extract/merge/refactor jwt repositories. Will be part of https://github.com/WalletConnect/WalletConnectKotlinV2/issues/583
internal class InviteKeyJwtRepository {

    fun generateInviteKeyJWT(invitePublicKey: String, identityKeyPair: Pair<PublicKey, PrivateKey>, keyserverUrl: String, account: AccountId): String {
        val (publicKey, privateKey) = identityKeyPair
        val privateKeyParameters = Ed25519PrivateKeyParameters(privateKey.keyAsBytes)

        val issuer = encodeIss(publicKey.keyAsBytes)
        val issuedAt = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
        val expiration = jwtExp(issuedAt)
        val pkh = encodeDidPkh(account)
        val payload = JwtClaims.InviteKey(issuer = issuer, subject = invitePublicKey, audience = keyserverUrl, issuedAt = issuedAt, expiration = expiration, pkh = pkh)
        val data = encodeData(JwtHeader.EdDSA, payload).encodeToByteArray()
        val signature = Ed25519Signer().run {
            init(true, privateKeyParameters)
            update(data, 0, data.size)
            generateSignature()
        }

        return encodeJWT(JwtHeader.EdDSA, payload, signature)
    }

    private fun encodeJWT(header: JwtHeader, payload: JwtClaims.InviteKey, signature: ByteArray): String {
        return listOf(encodeJSON(header), encodeJSON(payload), encodeByteArray(signature)).joinToString(JWT_DELIMITER)
    }

    private fun encodeData(header: JwtHeader, payload: JwtClaims.InviteKey): String {
        return listOf(encodeJSON(header), encodeJSON(payload)).joinToString(JWT_DELIMITER)
    }

    private fun <T> encodeJSON(jsonObj: T): String {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val jsonString = moshi.adapter<T>(jsonObj!!::class.java).toJson(jsonObj)
        val jsonByteArray = jsonString.toByteArray(Charsets.UTF_8)

        return encodeByteArray(jsonByteArray)
    }

    @Suppress("NewApi")
    fun encodeByteArray(signature: ByteArray): String {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signature)
    }

    private fun encodeIss(publicKey: ByteArray): String {
        val header: ByteArray = Base58.decode(MULTICODEC_ED25519_HEADER)
        val multicodec = Multibase.encode(Multibase.Base.Base58BTC, header + publicKey)

        return listOf(DID_PREFIX, DID_METHOD_KEY, multicodec).joinToString(DID_DELIMITER)
    }

    private fun jwtExp(issuedAt: Long): Long {
        return issuedAt + TimeUnit.SECONDS.convert(1, TimeUnit.DAYS)
    }

    private fun encodeDidPkh(accountId: AccountId): String {
        return listOf(DID_PREFIX, DID_METHOD_PKH, accountId.value).joinToString(DID_DELIMITER)
    }

    private companion object {
        const val JWT_DELIMITER = "."
        const val DID_DELIMITER = ":"
        const val DID_PREFIX = "did"
        const val DID_METHOD_KEY = "key"
        const val DID_METHOD_PKH = "pkh"
        const val MULTICODEC_ED25519_HEADER = "K36"
    }
}

