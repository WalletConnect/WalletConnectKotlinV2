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
import com.walletconnect.notify.data.wellknown.config.NotifyConfigDTO
import com.walletconnect.notify.data.wellknown.did.DidJsonDTO
import com.walletconnect.notify.data.wellknown.did.VerificationMethodDTO
import com.walletconnect.notify.engine.domain.RegisterIdentityAndReturnDidJwtInteractor
import com.walletconnect.util.bytesToHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.net.URL

typealias DidJsonPublicKeyPair = Pair<PublicKey, PublicKey>

internal class SubscribeToDappUseCase(
    private val serializer: JsonRpcSerializer,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val subscriptionRepository: SubscriptionRepository,
    private val crypto: KeyManagementRepository,
    private val explorerRepository: ExplorerRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val registerIdentityAndReturnDidJwt: RegisterIdentityAndReturnDidJwtInteractor,
    private val logger: Logger,
) : SubscribeToDappUseCaseInterface {

    override suspend fun subscribeToDapp(dappUri: Uri, account: String, onSign: (String) -> Cacao.Signature?, onSuccess: (Long, DidJwt) -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        val dappWellKnownProperties: Result<Pair<DidJsonPublicKeyPair, List<NotificationScope.Remote>>> = runCatching {
            extractPublicKeysFromDidJson(dappUri).getOrThrow() to extractNotificationScopeFromConfigJson(dappUri).getOrThrow()
        }

        dappWellKnownProperties.fold(
            onSuccess = { (dappPublicKeys, dappScopes) ->
                val (dappPublicKey, authenticationPublicKey) = dappPublicKeys   // TODO: Use authenticationPublicKey
                val subscribeTopic = Topic(sha256(dappPublicKey.keyAsBytes))

                if (subscriptionRepository.isAlreadyRequested(account, subscribeTopic.value)) return@fold onFailure(IllegalStateException("Account: $account is already subscribed to dapp: $dappUri"))

                val selfPublicKey = crypto.generateAndStoreX25519KeyPair()
                val responseTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, dappPublicKey)
                val dappMetaData: AppMetaData = getDappMetaData(dappUri).getOrElse {
                    return@fold onFailure(it)
                }

                val didJwt = registerIdentityAndReturnDidJwt.subscriptionRequest(AccountId(account), dappUri.toString(), dappScopes.map { it.name }, onSign, onFailure).getOrElse { error ->
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

    private suspend fun extractPublicKeysFromDidJson(dappUri: Uri): Result<DidJsonPublicKeyPair> = withContext(Dispatchers.IO) {
        val didJsonDappUri = generateAppropriateUri(dappUri, DID_JSON)

        val didJsonResult = didJsonDappUri.runCatching {
            // Get the did.json from the dapp
            URL(this.toString()).openStream().bufferedReader().use { it.readText() }
        }.mapCatching { wellKnownDidJsonString ->
            // Parse the did.json
            serializer.tryDeserialize<DidJsonDTO>(wellKnownDidJsonString)
                ?: throw Exception("Failed to parse $DID_JSON. Check that the $DID_JSON matches the specs.")
        }

        val keyAgreementPublicKey = didJsonResult
            .takeIf {
                didJsonResult.getOrNull()?.keyAgreement?.isNotEmpty() == true
            }?.mapCatching { didJsonDto ->
                didJsonDto.keyAgreement.first() to didJsonDto
            }?.mapCatching { (id, didJson) ->
                extractPublicKey(id, didJson.verificationMethod)
            } ?: Result.failure(Exception("Key Agreement is missing from $DID_JSON. Check that the $DID_JSON matches the specs."))

        // TODO: Re-implement after testing
        val authenticationPublicKey = /*Result.success(PublicKey(""))*/didJsonResult
            .takeIf {
                didJsonResult.getOrNull()?.authentication?.isNotEmpty() == true
            }?.mapCatching { didJsonDto ->
                didJsonDto.authentication.first() to didJsonDto
            }?.mapCatching { (controllerKey, didJson) ->
                extractPublicKey(controllerKey, didJson.verificationMethod)
            } ?: Result.failure(Exception("Authentication is missing from $DID_JSON. Check that the $DID_JSON matches the specs."))

        return@withContext runCatching {
            keyAgreementPublicKey.getOrThrow() to authenticationPublicKey.getOrThrow()
        }

//        val wellKnownDidJsonString = URL(didJsonDappUri.toString()).openStream().bufferedReader().use { it.readText() }
//        val didJson = serializer.tryDeserialize<com.walletconnect.notify.data.wellknown.did.DidJsonDTO>(wellKnownDidJsonString) ?: return@withContext Result.failure(Exception("Failed to parse $DID_JSON"))
//        val verificationKey = didJson.keyAgreement.first()

//        extractVerificationKey(verificationKey, didJsonResult.verificationMethod)
//        val jwkPublicKey = didJsonResult.verificationMethod.first { it.id == verificationKey }.publicKeyJwk.x

//        val replacedJwk = jwkPublicKey.replace("-", "+").replace("_", "/")
//        val publicKey = Base64.decode(replacedJwk, Base64.DEFAULT).bytesToHex()
//        Result.success(PublicKey(publicKey) to PublicKey(""))
    }

    private suspend fun extractNotificationScopeFromConfigJson(dappUri: Uri): Result<List<NotificationScope.Remote>> = withContext(Dispatchers.IO) {
        val notifyConfigDappUri = generateAppropriateUri(dappUri, WC_NOTIFY_CONFIG_JSON)

        return@withContext /*Result.success(emptyList())*/notifyConfigDappUri.runCatching {
            // Get the did.json from the dapp
            URL(this.toString()).openStream().bufferedReader().use { it.readText() }
        }.mapCatching { wellKnownNotifyConfigString ->
            // Parse the did.json
            serializer.tryDeserialize<NotifyConfigDTO>(wellKnownNotifyConfigString)
                ?: throw Exception("Failed to parse $WC_NOTIFY_CONFIG_JSON. Check that the $$WC_NOTIFY_CONFIG_JSON file matches the specs")
        }.mapCatching { notifyConfig ->
            notifyConfig.types.map { typeDTO ->
                NotificationScope.Remote(
                    name = typeDTO.name,
                    description = typeDTO.description
                )
            }
        }

//        val wellKnownNotifyConfigString = URL(notifyConfigDappUri.toString()).openStream().bufferedReader().use { it.readText() }
//        val notifyConfig = serializer.tryDeserialize<NotifyConfigDTO>(wellKnownNotifyConfigString) ?: return@withContext Result.failure(Exception("Failed to parse $WC_NOTIFY_CONFIG_JSON"))
//        val notificationScopeRemote = notifyConfig.types.map { typeDTO ->
//            NotificationScope.Remote(
//                name = typeDTO.name,
//                description = typeDTO.description
//            )
//        }

//        Result.success(notificationScopeRemote)
    }

    private fun extractPublicKey(id: String, verificationMethodList: List<VerificationMethodDTO>): PublicKey {
        val verificationMethod = verificationMethodList.firstOrNull { verificationMethod -> verificationMethod.id == id } ?: throw Exception("Failed to find verification key")
        val jwkPublicKey = verificationMethod.publicKeyJwk.x
        val replacedJwk = jwkPublicKey.replace("-", "+").replace("_", "/")
        val publicKey = Base64.decode(replacedJwk, Base64.DEFAULT).bytesToHex()

        return PublicKey(publicKey)
    }

    private fun generateAppropriateUri(dappUri: Uri, path: String): Uri =
        if (dappUri.path?.contains(path) == false) {
            dappUri.buildUpon().appendPath(path).build()
        } else {
            dappUri
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
        const val WC_NOTIFY_CONFIG_JSON = ".well-known/wc-notify-config.json"
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