package com.walletconnect.android.keyserver.domain

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.*
import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.jwt.did.EncodeIdentityKeyDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.jwt.did.encodeDidJwt
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.DidJwt
import com.walletconnect.android.internal.common.model.MissingKeyException
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.signing.cacao.CacaoType
import com.walletconnect.android.internal.common.signing.cacao.CacaoVerifier
import com.walletconnect.android.internal.common.signing.cacao.toCAIP122Message
import com.walletconnect.android.internal.common.storage.IdentitiesStorageRepository
import com.walletconnect.android.internal.utils.getIdentityTag
import com.walletconnect.android.keyserver.domain.use_case.RegisterIdentityUseCase
import com.walletconnect.android.keyserver.domain.use_case.ResolveIdentityUseCase
import com.walletconnect.android.keyserver.domain.use_case.UnregisterIdentityUseCase
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.util.jwt.DID_DELIMITER
import com.walletconnect.foundation.util.jwt.decodeDidPkh
import com.walletconnect.foundation.util.jwt.encodeDidPkh
import com.walletconnect.foundation.util.jwt.encodeEd25519DidKey
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.randomBytes
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

class IdentitiesInteractor(
    private val identitiesRepository: IdentitiesStorageRepository,
    private val resolveIdentityUseCase: ResolveIdentityUseCase,
    private val registerIdentityUseCase: RegisterIdentityUseCase,
    private val unregisterIdentityUseCase: UnregisterIdentityUseCase,
    private val projectId: ProjectId,
    private val keyManagementRepository: KeyManagementRepository,
) {
    fun getIdentityKeyPair(accountId: AccountId): Pair<PublicKey, PrivateKey> = keyManagementRepository.getKeyPair(getIdentityPublicKey(accountId))

    suspend fun registerIdentity(accountId: AccountId, keyserverUrl: String, onSign: (String) -> Cacao.Signature?): Result<PublicKey> = try {
        if (!accountId.isValid()) throw InvalidAccountIdException(accountId)
        val storedPublicKey = getIdentityPublicKey(accountId)
        Result.success(storedPublicKey)
    } catch (e: MissingKeyException) {
        val identityPublicKey = generateAndStoreIdentityKeyPair()
        registerIdentityKeyInKeyserver(accountId, keyserverUrl, identityPublicKey, onSign)
            .map { identityPublicKey }
            .onSuccess { storeIdentityPublicKey(identityPublicKey, accountId) }
    }

    suspend fun unregisterIdentity(accountId: AccountId, keyserverUrl: String): Result<PublicKey> = try {
        if (!accountId.isValid()) throw InvalidAccountIdException(accountId)
        val storedKeyPair = getIdentityKeyPair(accountId)
        val (storedPublicKey, _) = storedKeyPair
        unregisterIdentityKeyInKeyserver(accountId, keyserverUrl, storedKeyPair)
            .map { storedPublicKey }
            .onSuccess { removeIdentityKeyPair(storedPublicKey, accountId) }
    } catch (e: MissingKeyException) {
        throw AccountHasNoIdentityStored(accountId)
    }

    private suspend fun resolveIdentity(identityKey: String): Result<AccountId> = resolveIdentityLocally(identityKey).recover { resolveAndStoreIdentityRemotely(identityKey).getOrThrow() }
    suspend fun resolveIdentityDidKey(identityDidKey: String): Result<AccountId> = resolveIdentity(identityDidKey.split(DID_DELIMITER).last())

    private suspend fun resolveIdentityLocally(identityKey: String): Result<AccountId> = runCatching { identitiesRepository.getAccountId(identityKey) }

    private suspend fun resolveAndStoreIdentityRemotely(identityKey: String) = resolveIdentityUseCase(identityKey).mapCatching { response ->
        if (!CacaoVerifier(projectId).verify(response.cacao)) throw InvalidIdentityCacao()
        val accountId = AccountId(decodeDidPkh(response.cacao.payload.iss))
        identitiesRepository.insertIdentity(identityKey, accountId)
        accountId
    }

    private fun getIdentityPublicKey(accountId: AccountId): PublicKey = keyManagementRepository.getPublicKey(accountId.getIdentityTag())

    private fun storeIdentityPublicKey(publicKey: PublicKey, accountId: AccountId) = keyManagementRepository.setKey(publicKey, accountId.getIdentityTag())

    private fun removeIdentityKeyPair(publicKey: PublicKey, accountId: AccountId) {
        keyManagementRepository.removeKeys(accountId.getIdentityTag())
        keyManagementRepository.removeKeys(publicKey.keyAsHex)
    }

    private fun generateAndStoreIdentityKeyPair(): PublicKey = keyManagementRepository.generateAndStoreEd25519KeyPair()

    private suspend fun registerIdentityKeyInKeyserver(accountId: AccountId, keyserverUrl: String, identityKey: PublicKey, onSign: (String) -> Cacao.Signature?): Result<Unit> =
        registerIdentityUseCase(generateCacao(accountId, keyserverUrl, identityKey, onSign).getOrThrow())

    private suspend fun unregisterIdentityKeyInKeyserver(accountId: AccountId, keyserverUrl: String, identityKeyPair: Pair<PublicKey, PrivateKey>): Result<Unit> =
        unregisterIdentityUseCase(generateUnregisterIdAuth(accountId, keyserverUrl, identityKeyPair).getOrThrow().value)

    private fun generateCacao(accountId: AccountId, keyserverUrl: String, identityKey: PublicKey, onSign: (String) -> Cacao.Signature?): Result<Cacao> {
        val payload = generatePayload(accountId, keyserverUrl, identityKey).getOrThrow()
        val message = payload.toCAIP122Message()
        val signature = onSign(message) ?: throw UserRejectedSigning()
        return Result.success(Cacao(CacaoType.EIP4361.toHeader(), payload, signature))
    }

    private fun generateUnregisterIdAuth(accountId: AccountId, keyserverUrl: String, identityKeyPair: Pair<PublicKey, PrivateKey>): Result<DidJwt> {
        val (identityPublicKey, identityPrivateKey) = identityKeyPair
        return encodeDidJwt(identityPrivateKey, EncodeIdentityKeyDidJwtPayloadUseCase(accountId), EncodeDidJwtPayloadUseCase.Params(identityPublicKey, keyserverUrl))
    }

    private fun String.toDomain(): Result<String> = runCatching {
        val uri = URI(this)
        val domain: String = uri.host
        if (domain.startsWith("www.")) domain.substring(4) else domain
    }

    private fun generatePayload(accountId: AccountId, keyserverUrl: String, identityKey: PublicKey): Result<Cacao.Payload> = Result.success(
        Cacao.Payload(
            iss = encodeDidPkh(accountId.value),
            domain = keyserverUrl.toDomain().getOrThrow(),
            aud = keyserverUrl,
            version = Cacao.Payload.CURRENT_VERSION,
            nonce = randomBytes(NONCE_SIZE).bytesToHex(),
            iat = SimpleDateFormat(Cacao.Payload.ISO_8601_PATTERN, Locale.getDefault()).format(Calendar.getInstance().time),
            nbf = null,
            exp = null,
            statement = null,
            requestId = null,
            resources = listOf(encodeEd25519DidKey(identityKey.keyAsBytes))
        )
    ).recover { throw UnableToExtractDomainException(keyserverUrl) } // mapping to internal error

    companion object {
        const val NONCE_SIZE = 32
    }
}