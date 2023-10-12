package com.walletconnect.android.keyserver.domain

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.AccountHasNoCacaoPayloadStored
import com.walletconnect.android.internal.common.exception.AccountHasNoIdentityStored
import com.walletconnect.android.internal.common.exception.AccountHasDifferentStatementStored
import com.walletconnect.android.internal.common.exception.InvalidAccountIdException
import com.walletconnect.android.internal.common.exception.InvalidIdentityCacao
import com.walletconnect.android.internal.common.exception.UserRejectedSigning
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class IdentitiesInteractor(
    private val identitiesRepository: IdentitiesStorageRepository,
    private val resolveIdentityUseCase: ResolveIdentityUseCase,
    private val registerIdentityUseCase: RegisterIdentityUseCase,
    private val unregisterIdentityUseCase: UnregisterIdentityUseCase,
    private val projectId: ProjectId,
    private val keyManagementRepository: KeyManagementRepository,
) {
    fun getIdentityKeyPair(accountId: AccountId): Pair<PublicKey, PrivateKey> = keyManagementRepository.getKeyPair(getIdentityPublicKey(accountId))

    suspend fun registerIdentity(accountId: AccountId, statement: String, domain: String, resources: List<String>, keyserverUrl: String, onSign: (String) -> Cacao.Signature?): Result<PublicKey> =
        getAlreadyRegisteredIdentity(accountId, statement, domain, resources)
            .recoverCatching { exception ->
                when (exception) {
                    is MissingKeyException -> handleNotYetGeneratedIdentities(accountId, statement, domain, resources, onSign).getOrThrow()
                    is AccountHasNoCacaoPayloadStored, is AccountHasDifferentStatementStored -> handleIdentitiesOutdatedStatements(accountId, statement, domain, resources, keyserverUrl, onSign).getOrThrow()
                    else -> throw exception
                }
            }

    private suspend fun getAlreadyRegisteredIdentity(accountId: AccountId, statement: String, domain: String, resources: List<String>): Result<PublicKey> {
        if (!accountId.isValid()) throw InvalidAccountIdException(accountId)
        return runCatching {
            val storedPublicKey = getIdentityPublicKey(accountId)
            val cacaoPayload = identitiesRepository.getCacaoPayloadByIdentity(storedPublicKey.keyAsHex) ?: throw AccountHasNoCacaoPayloadStored(accountId)
            val generatedPayload = generatePayload(accountId, storedPublicKey, statement, domain, resources).getOrThrow()
            if (cacaoPayload.statement != generatedPayload.statement) throw AccountHasDifferentStatementStored(accountId)
            storedPublicKey
        }
    }

    private suspend fun handleNotYetGeneratedIdentities(accountId: AccountId, statement: String, domain: String, resources: List<String>, onSign: (String) -> Cacao.Signature?): Result<PublicKey> {
        val identityPublicKey = generateAndStoreIdentityKeyPair()
        return registerIdentityKeyInKeyserver(accountId, identityPublicKey, statement, domain, resources, onSign)
            .map { identityPublicKey }
            .onSuccess { storeIdentityPublicKey(identityPublicKey, accountId) }
    }

    private suspend fun handleIdentitiesOutdatedStatements(
        accountId: AccountId, statement: String, domain: String, resources: List<String>, keyserverUrl: String, onSign: (String) -> Cacao.Signature?,
    ): Result<PublicKey> {
        val storedKeyPair = getIdentityKeyPair(accountId)
        val (storedPublicKey, _) = storedKeyPair
        return unregisterIdentityKeyInKeyserver(accountId, keyserverUrl, storedKeyPair)
            .map { storedPublicKey }
            .onSuccess { registerIdentityKeyInKeyserver(accountId, storedPublicKey, statement, domain, resources, onSign) }
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
        identitiesRepository.insertIdentity(identityKey, accountId, response.cacao.payload, isMine = false)
        accountId
    }

    private fun getIdentityPublicKey(accountId: AccountId): PublicKey = keyManagementRepository.getPublicKey(accountId.getIdentityTag())

    private fun storeIdentityPublicKey(publicKey: PublicKey, accountId: AccountId) {
        keyManagementRepository.setKey(publicKey, accountId.getIdentityTag())
    }

    private fun removeIdentityKeyPair(publicKey: PublicKey, accountId: AccountId) {
        keyManagementRepository.removeKeys(accountId.getIdentityTag())
        keyManagementRepository.removeKeys(publicKey.keyAsHex)
    }

    private fun generateAndStoreIdentityKeyPair(): PublicKey = keyManagementRepository.generateAndStoreEd25519KeyPair()

    private suspend fun registerIdentityKeyInKeyserver(
        accountId: AccountId,
        identityKey: PublicKey,
        statement: String,
        domain: String,
        resources: List<String>,
        onSign: (String) -> Cacao.Signature?,
    ): Result<Unit> {
        val cacao = generateCacao(accountId, identityKey, statement, domain, resources, onSign).getOrThrow()
        return registerIdentityUseCase(cacao).onSuccess {
            identitiesRepository.insertIdentity(identityKey.keyAsHex, accountId, cacao.payload, isMine = true)
        }
    }

    private suspend fun unregisterIdentityKeyInKeyserver(accountId: AccountId, keyserverUrl: String, identityKeyPair: Pair<PublicKey, PrivateKey>): Result<Unit> {
        return unregisterIdentityUseCase(generateUnregisterIdAuth(accountId, keyserverUrl, identityKeyPair).getOrThrow().value).onSuccess {
            identitiesRepository.removeIdentity(identityKeyPair.first.keyAsHex)
        }
    }

    private fun generateCacao(accountId: AccountId, identityKey: PublicKey, statement: String, domain: String, resources: List<String>, onSign: (String) -> Cacao.Signature?): Result<Cacao> {
        val payload = generatePayload(accountId, identityKey, statement, domain, resources).getOrThrow()
        val message = payload.toCAIP122Message()
        val signature = onSign(message) ?: throw UserRejectedSigning()
        return Result.success(Cacao(CacaoType.EIP4361.toHeader(), payload, signature))
    }

    private fun generateUnregisterIdAuth(accountId: AccountId, keyserverUrl: String, identityKeyPair: Pair<PublicKey, PrivateKey>): Result<DidJwt> {
        val (identityPublicKey, identityPrivateKey) = identityKeyPair
        return encodeDidJwt(identityPrivateKey, EncodeIdentityKeyDidJwtPayloadUseCase(accountId), EncodeDidJwtPayloadUseCase.Params(identityPublicKey, keyserverUrl))
    }


    private fun generatePayload(accountId: AccountId, identityKey: PublicKey, statement: String, domain: String, resources: List<String>): Result<Cacao.Payload> = Result.success(
        Cacao.Payload(
            iss = encodeDidPkh(accountId.value),
            domain = domain,
            aud = encodeEd25519DidKey(identityKey.keyAsBytes),
            version = Cacao.Payload.CURRENT_VERSION,
            nonce = randomBytes(NONCE_SIZE).bytesToHex(),
            iat = SimpleDateFormat(Cacao.Payload.ISO_8601_PATTERN, Locale.getDefault()).format(Calendar.getInstance().time),
            nbf = null,
            exp = null,
            statement = statement,
            requestId = null,
            resources = resources
        )
    )

    companion object {
        const val NONCE_SIZE = 32
    }
}