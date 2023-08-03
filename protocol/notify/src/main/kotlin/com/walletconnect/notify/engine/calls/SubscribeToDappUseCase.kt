@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import android.net.Uri
import android.util.Base64
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.explorer.ExplorerRepository
import com.walletconnect.android.internal.common.explorer.data.model.Listing
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.DidJwt
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Participants
import com.walletconnect.android.internal.common.model.Redirect
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.NotifyParams
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.notify.common.calcExpiry
import com.walletconnect.notify.common.model.NotificationScope
import com.walletconnect.notify.common.model.NotifyRpc
import com.walletconnect.notify.data.storage.SubscriptionRepository
import com.walletconnect.notify.data.wellknown.did.DidJsonDTO
import com.walletconnect.notify.engine.domain.ExtractNotifyConfigUseCase
import com.walletconnect.notify.engine.domain.RegisterIdentityAndReturnDidJwtUseCaseInterface
import com.walletconnect.util.bytesToHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.net.URL

internal class SubscribeToDappUseCase(
    private val serializer: JsonRpcSerializer,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val extractNotifyConfigUseCase: ExtractNotifyConfigUseCase,
    private val subscriptionRepository: SubscriptionRepository,
    private val crypto: KeyManagementRepository,
    private val explorerRepository: ExplorerRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val registerIdentityAndReturnDidJwt: RegisterIdentityAndReturnDidJwtUseCaseInterface,
    private val logger: Logger,
) : SubscribeToDappUseCaseInterface {

    override suspend fun subscribeToDapp(dappUri: Uri, account: String, onSign: (String) -> Cacao.Signature?, onSuccess: (Long, DidJwt) -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        val dappWellKnownProperties: Result<Pair<PublicKey, List<NotificationScope.Remote>>> = runCatching {
            extractDidJson(dappUri).getOrThrow() to extractNotifyConfigUseCase(dappUri).getOrThrow()
        }

        dappWellKnownProperties.fold(
            onSuccess = { (dappPublicKey, dappScopes) ->
                val subscribeTopic = Topic(sha256(dappPublicKey.keyAsBytes))

                if (subscriptionRepository.isAlreadyRequested(account, subscribeTopic.value)) return@fold onFailure(IllegalStateException("Account: $account is already subscribed to dapp: $dappUri"))

                val selfPublicKey = crypto.generateAndStoreX25519KeyPair()
                val responseTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, dappPublicKey)
                val dappMetaData: AppMetaData = getDappMetaData(dappUri).getOrElse {
                    return@fold onFailure(it)
                }

                val didJwt = registerIdentityAndReturnDidJwt(AccountId(account), dappUri.toString(), dappScopes.map { it.name }, onSign, onFailure).getOrElse { error ->
                    return@fold onFailure(error)
                }
                val params = NotifyParams.SubscribeParams(didJwt.value)
                val request = NotifyRpc.NotifySubscribe(params = params)
                val irnParams = IrnParams(Tags.NOTIFY_SUBSCRIBE, Ttl(DAY_IN_SECONDS))

                runCatching<Unit> {
                    subscriptionRepository.insertOrAbortRequestedSubscription(
                        requestId = request.id,
                        subscribeTopic = subscribeTopic.value,
                        responseTopic = responseTopic.value,
                        account = account,
                        mapOfScope = dappScopes.associate { scope -> scope.name to Pair(scope.description, true) },
                        expiry = calcExpiry().seconds,
                    )
                }.mapCatching {
                    metadataStorageRepository.insertOrAbortMetadata(
                        topic = responseTopic,
                        appMetaData = dappMetaData,
                        appMetaDataType = AppMetaDataType.PEER,
                    )
                }.onFailure { error ->
                    logger.error("Cannot insert: ${error.message}")
                    return@fold onFailure(error)
                }

                jsonRpcInteractor.subscribe(responseTopic) { error ->
                    return@subscribe onFailure(error)
                }

                jsonRpcInteractor.publishJsonRpcRequest(
                    topic = subscribeTopic,
                    params = irnParams,
                    payload = request,
                    envelopeType = EnvelopeType.ONE,
                    participants = Participants(selfPublicKey, dappPublicKey),
                    onSuccess = {
                        onSuccess(request.id, didJwt)
                    },
                    onFailure = { error ->
                        onFailure(error)
                    }
                )
            },
            onFailure = { error -> return@supervisorScope onFailure(error) }
        )
    }

    private suspend fun extractDidJson(dappUri: Uri): Result<PublicKey> = withContext(Dispatchers.IO) {
        val didJsonDappUri = dappUri.run {
            if (this.path?.contains(DID_JSON) == false) {
                this.buildUpon().appendPath(DID_JSON).build()
            } else {
                this
            }
        }

        val wellKnownDidJsonString = URL(didJsonDappUri.toString()).openStream().bufferedReader().use { it.readText() }
        val didJson = serializer.tryDeserialize<DidJsonDTO>(wellKnownDidJsonString) ?: return@withContext Result.failure(Exception("Failed to parse $DID_JSON"))
        val verificationKey = didJson.keyAgreement.first()
        val jwkPublicKey = didJson.verificationMethod.first { it.id == verificationKey }.publicKeyJwk.x
        val replacedJwk = jwkPublicKey.replace("-", "+").replace("_", "/")
        val publicKey = Base64.decode(replacedJwk, Base64.DEFAULT).bytesToHex()
        Result.success(PublicKey(publicKey))
    }

    private suspend fun getDappMetaData(dappUri: Uri) = withContext(Dispatchers.IO) {
        val listOfDappHomepages = runCatching {
            explorerRepository.getAllDapps().listings.associateBy { listing -> listing.homepage }
        }.getOrElse { error ->
            return@withContext Result.failure(error)
        }

        val (dappHomepageUri: Uri, dappListing: Listing) = listOfDappHomepages.entries.filter { (_, dappListing) ->
            dappListing.description != null
        }.firstOrNull { (dappHomepageUri, _) ->
            dappHomepageUri.host != null && dappHomepageUri.host!!.contains(dappUri.host!!)
        } ?: return@withContext Result.failure<AppMetaData>(IllegalArgumentException("Unable to find dapp listing for $dappUri"))

        return@withContext Result.success(
            AppMetaData(
                name = dappListing.name,
                description = dappListing.description!!,
                icons = listOf(dappListing.imageUrl.sm, dappListing.imageUrl.md, dappListing.imageUrl.lg),
                url = dappHomepageUri.toString(),
                redirect = Redirect(dappListing.app.android)
            )
        )
    }

    private companion object {
        const val DID_JSON = ".well-known/did.json"
    }
}

internal interface SubscribeToDappUseCaseInterface {

    suspend fun subscribeToDapp(
        dappUri: Uri,
        account: String,
        onSign: (String) -> Cacao.Signature?,
        onSuccess: (Long, DidJwt) -> Unit,
        onFailure: (Throwable) -> Unit,
    )
}