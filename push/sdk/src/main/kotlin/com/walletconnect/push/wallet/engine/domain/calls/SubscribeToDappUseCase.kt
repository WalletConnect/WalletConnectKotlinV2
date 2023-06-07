@file:JvmSynthetic

package com.walletconnect.push.wallet.engine.domain.calls

import android.net.Uri
import android.util.Base64
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.explorer.ExplorerRepository
import com.walletconnect.android.internal.common.explorer.data.model.Listing
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Participants
import com.walletconnect.android.internal.common.model.Redirect
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.PushParams
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.push.common.calcExpiry
import com.walletconnect.push.common.data.storage.SubscriptionStorageRepository
import com.walletconnect.push.common.domain.ExtractPushConfigUseCase
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.model.PushRpc
import com.walletconnect.push.wallet.data.wellknown.did.DidJsonDTO
import com.walletconnect.util.bytesToHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.net.URL

internal class SubscribeToDappUseCase(
    private val serializer: JsonRpcSerializer,
    private val crypto: KeyManagementRepository,
    private val explorerRepository: ExplorerRepository,
    private val registerIdentityAndReturnDidJwtUseCase: RegisterIdentityAndReturnDidJwtUseCase,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val subscriptionStorageRepository: SubscriptionStorageRepository,
    private val extractPushConfigUseCase: ExtractPushConfigUseCase,
): SubscribeToDappUseCaseInterface {

    override suspend fun subscribeToDapp(dappUri: Uri, account: String, onSign: (String) -> Cacao.Signature?, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        val dappWellKnownProperties: Result<Pair<PublicKey, List<EngineDO.PushScope.Remote>>> = runCatching {
            extractDidJson(dappUri).getOrThrow() to extractPushConfigUseCase(dappUri).getOrThrow()
        }

        dappWellKnownProperties.fold(
            onSuccess = { (dappPublicKey, dappScope) -> createSubscription(dappUri, account, onSign, dappPublicKey, dappScope, onSuccess, onFailure) },
            onFailure = { error -> return@supervisorScope onFailure(error) }
        )
    }

    private suspend fun createSubscription(dappUri: Uri, account: String, onSign: (String) -> Cacao.Signature?, dappPublicKey: PublicKey, dappScopes: List<EngineDO.PushScope.Remote>, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        val subscribeTopic = Topic(sha256(dappPublicKey.keyAsBytes))
        val selfPublicKey = crypto.generateAndStoreX25519KeyPair()
        val responseTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, dappPublicKey)
        val dappMetaData: AppMetaData = withContext(Dispatchers.IO) {
            // Fetch dapp metadata from explorer api
            val listOfDappHomepages = runCatching {
                explorerRepository.getAllDapps().listings.associateBy { listing -> listing.homepage.host }
            }.getOrElse { error ->
                return@withContext Result.failure(error)
            }

            // Find dapp metadata for dapp uri
            val (dappHomepageHost: String?, dappListing: Listing) = listOfDappHomepages.entries.filter { (dappHomepageHost, dappListing) ->
                dappHomepageHost != null && dappListing.description != null
            }.firstOrNull { (dappHomepageHost, _) ->
                dappHomepageHost != null && dappHomepageHost.contains(dappUri.host!!)
            } ?: return@withContext Result.failure<AppMetaData>(IllegalArgumentException("Unable to find dapp listing for $dappUri"))

            // Return dapp metadata
            return@withContext Result.success(
                AppMetaData(
                    name = dappListing.name,
                    description = dappListing.description!!,
                    icons = listOf(dappListing.imageUrl.sm, dappListing.imageUrl.md, dappListing.imageUrl.lg),
                    url = dappHomepageHost!!,
                    redirect = Redirect(dappListing.app.android)
                )
            )
        }.getOrElse {
            return onFailure(it)
        }

        val didJwt = registerIdentityAndReturnDidJwtUseCase(AccountId(account), dappUri.toString(), dappScopes.map { it.name }, onSign, onFailure).getOrElse { error ->
            return onFailure(error)
        }
        val params = PushParams.SubscribeParams(didJwt.value)
        val request = PushRpc.PushSubscribe(params = params)
        val irnParams = IrnParams(Tags.PUSH_SUBSCRIBE, Ttl(DAY_IN_SECONDS))

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
                runBlocking {
                    subscriptionStorageRepository.insertSubscription(
                        requestId = request.id,
                        keyAgreementTopic = responseTopic.value,
                        responseTopic = subscribeTopic.value,
                        peerPublicKeyAsHex = null,
                        subscriptionTopic = null,
                        account = account,
                        relayProtocol = null,
                        relayData = null,
                        name = dappMetaData.name,
                        description = dappMetaData.description,
                        url = dappMetaData.url,
                        icons = dappMetaData.icons,
                        native = dappMetaData.redirect?.native,
                        didJwt = didJwt.value,
                        mapOfScope = dappScopes.associate { scope -> scope.name to Pair(scope.description, true) },
                        expiry = calcExpiry()
                    )
                }

                onSuccess()
            },
            onFailure = { error ->
                onFailure(error)
            }
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
        val didJson = serializer.tryDeserialize<DidJsonDTO>(wellKnownDidJsonString) ?: return@withContext Result.failure(Exception("Failed to parse well-known/did.json"))
        val verificationKey = didJson.keyAgreement.first()
        val jwkPublicKey = didJson.verificationMethod.first { it.id == verificationKey }.publicKeyJwk.x
        val replacedJwk = jwkPublicKey.replace("-", "+").replace("_", "/")
        val publicKey = Base64.decode(replacedJwk, Base64.DEFAULT).bytesToHex()
        Result.success(PublicKey(publicKey))
    }

    private companion object {
        const val DID_JSON = ".well-known/did.json"
    }
}

internal interface SubscribeToDappUseCaseInterface {
    suspend fun subscribeToDapp(dappUri: Uri, account: String, onSign: (String) -> Cacao.Signature?, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}