@file:JvmSynthetic

package com.walletconnect.notify.engine.domain

import android.net.Uri
import android.util.Base64
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.notify.data.wellknown.did.DidJsonDTO
import com.walletconnect.notify.data.wellknown.did.VerificationMethodDTO
import com.walletconnect.notify.engine.calls.DidJsonPublicKeyPair
import com.walletconnect.util.bytesToHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

internal class ExtractPublicKeysFromDidJsonUseCase(
    private val serializer: JsonRpcSerializer,
    private val generateAppropriateUri: GenerateAppropriateUriUseCase,
) {

    suspend operator fun invoke(dappUri: Uri): Result<DidJsonPublicKeyPair> = withContext(Dispatchers.IO) {
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
        val authenticationPublicKey = didJsonResult
            .takeIf {
                didJsonResult.getOrNull()?.authentication?.isNotEmpty() == true
            }?.mapCatching { didJsonDto ->
                didJsonDto.authentication.first() to didJsonDto
            }?.mapCatching { (id, didJson) ->
                extractPublicKey(id, didJson.verificationMethod)
            } ?: Result.failure(Exception("Authentication is missing from $DID_JSON. Check that the $DID_JSON matches the specs."))

        return@withContext runCatching {
            keyAgreementPublicKey.getOrThrow() to authenticationPublicKey.getOrThrow()
        }
    }

    private fun extractPublicKey(id: String, verificationMethodList: List<VerificationMethodDTO>): PublicKey {
        val verificationMethod = verificationMethodList.firstOrNull { verificationMethod -> verificationMethod.id == id } ?: throw Exception("Failed to find verification key")
        val jwkPublicKey = verificationMethod.publicKeyJwk.x
        val replacedJwk = jwkPublicKey.replace("-", "+").replace("_", "/")
        val publicKey = Base64.decode(replacedJwk, Base64.DEFAULT).bytesToHex()

        return PublicKey(publicKey)
    }

    private companion object {
        const val DID_JSON = ".well-known/did.json"
    }
}