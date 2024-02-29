@file:JvmName("ApprovedNamespacesUtils")

package com.walletconnect.sign.client.utils

import com.walletconnect.android.internal.common.model.Namespace
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.signing.cacao.Cacao.Payload.Companion.RECAPS_PREFIX
import com.walletconnect.android.internal.common.signing.cacao.CacaoType
import com.walletconnect.android.internal.common.signing.cacao.decodeReCaps
import com.walletconnect.android.internal.common.signing.cacao.parseReCaps
import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.mapper.toCacaoPayload
import com.walletconnect.sign.client.mapper.toCore
import com.walletconnect.sign.client.mapper.toProposalNamespacesVO
import com.walletconnect.sign.client.mapper.toSessionNamespacesVO
import com.walletconnect.sign.common.validator.SignValidator
import org.bouncycastle.util.encoders.Base64
import org.json.JSONArray
import org.json.JSONObject

fun generateApprovedNamespaces(
    proposal: Sign.Model.SessionProposal,
    supportedNamespaces: Map<String, Sign.Model.Namespace.Session>,
): Map<String, Sign.Model.Namespace.Session> {
    val supportedNamespacesVO = supportedNamespaces.toSessionNamespacesVO()
    val normalizedRequiredNamespaces = normalizeNamespaces(proposal.requiredNamespaces.toProposalNamespacesVO())
    val normalizedOptionalNamespaces = normalizeNamespaces(proposal.optionalNamespaces.toProposalNamespacesVO())

    SignValidator.validateProposalNamespaces(normalizedRequiredNamespaces) { error -> throw Exception(error.message) }
    SignValidator.validateProposalNamespaces(normalizedOptionalNamespaces) { error -> throw Exception(error.message) }
    SignValidator.validateSupportedNamespace(supportedNamespacesVO, normalizedRequiredNamespaces) { error -> throw Exception(error.message) }

    if (proposal.requiredNamespaces.isEmpty() && proposal.optionalNamespaces.isEmpty()) {
        return supportedNamespacesVO.toCore()
    }

    val approvedNamespaces = mutableMapOf<String, Namespace.Session>()
    normalizedRequiredNamespaces.forEach { (key, requiredNamespace) ->
        val chains = supportedNamespacesVO[key]?.chains?.filter { chain -> requiredNamespace.chains!!.contains(chain) } ?: emptyList()
        val methods = supportedNamespaces[key]?.methods?.filter { method -> requiredNamespace.methods.contains(method) } ?: emptyList()
        val events = supportedNamespaces[key]?.events?.filter { event -> requiredNamespace.events.contains(event) } ?: emptyList()
        val accounts = chains.flatMap { chain -> supportedNamespaces[key]?.accounts?.filter { account -> SignValidator.getChainFromAccount(account) == chain } ?: emptyList() }

        approvedNamespaces[key] = Namespace.Session(chains = chains, methods = methods, events = events, accounts = accounts)
    }

    normalizedOptionalNamespaces.forEach { (key, optionalNamespace) ->
        if (!supportedNamespaces.containsKey(key)) return@forEach
        val chains = supportedNamespacesVO[key]?.chains?.filter { chain -> optionalNamespace.chains!!.contains(chain) } ?: emptyList()
        val methods = supportedNamespaces[key]?.methods?.filter { method -> optionalNamespace.methods.contains(method) } ?: emptyList()
        val events = supportedNamespaces[key]?.events?.filter { event -> optionalNamespace.events.contains(event) } ?: emptyList()
        val accounts = chains.flatMap { chain -> supportedNamespaces[key]?.accounts?.filter { account -> SignValidator.getChainFromAccount(account) == chain } ?: emptyList() }

        approvedNamespaces[key] = Namespace.Session(
            chains = approvedNamespaces[key]?.chains?.plus(chains)?.distinct() ?: chains,
            methods = approvedNamespaces[key]?.methods?.plus(methods)?.distinct() ?: methods,
            events = approvedNamespaces[key]?.events?.plus(events)?.distinct() ?: events,
            accounts = approvedNamespaces[key]?.accounts?.plus(accounts)?.distinct() ?: accounts
        )
    }

    return approvedNamespaces.toCore()
}

internal fun normalizeNamespaces(namespaces: Map<String, Namespace.Proposal>): Map<String, Namespace.Proposal> {
    if (SignValidator.isNamespaceKeyRegexCompliant(namespaces)) return namespaces
    return mutableMapOf<String, Namespace.Proposal>().apply {
        namespaces.forEach { (key, namespace) ->
            val normalizedKey = normalizeKey(key)
            this[normalizedKey] = Namespace.Proposal(
                chains = getChains(normalizedKey).plus(getNamespaceChains(key, namespace)),
                methods = getMethods(normalizedKey).plus(namespace.methods),
                events = getEvents(normalizedKey).plus(namespace.events)
            )
        }
    }.toMap()
}

private fun getNamespaceChains(key: String, namespace: Namespace) = if (CoreValidator.isChainIdCAIP2Compliant(key)) listOf(key) else namespace.chains!!
private fun normalizeKey(key: String): String = if (CoreValidator.isChainIdCAIP2Compliant(key)) SignValidator.getNamespaceKeyFromChainId(key) else key
private fun MutableMap<String, Namespace.Proposal>.getChains(normalizedKey: String) = (this[normalizedKey]?.chains ?: emptyList())
private fun MutableMap<String, Namespace.Proposal>.getMethods(normalizedKey: String) = (this[normalizedKey]?.methods ?: emptyList())
private fun MutableMap<String, Namespace.Proposal>.getEvents(normalizedKey: String) = (this[normalizedKey]?.events ?: emptyList())

fun generateAuthObject(payload: Sign.Model.PayloadParams, issuer: String, signature: Sign.Model.Cacao.Signature): Sign.Model.Cacao {
    return Sign.Model.Cacao(
        header = Sign.Model.Cacao.Header(t = CacaoType.CAIP222.header),
        payload = payload.toCacaoPayload(issuer),
        signature = signature
    )
}

fun generateAuthPayloadParams(payloadParams: Sign.Model.PayloadParams, supportedChains: List<String>, supportedMethods: List<String>): Sign.Model.PayloadParams {
    val reCapsList: List<String>? = payloadParams.resources.decodeReCaps()?.filter { decoded -> decoded.contains("eip155") }

    if (reCapsList.isNullOrEmpty()) return payloadParams
    val sessionReCaps = reCapsList.parseReCaps()["eip155"]

    val requestedMethods = sessionReCaps!!.keys.map { key -> key.substringAfter('/') }
    val requestedChains = payloadParams.chains

    val sessionChains = requestedChains.intersect(supportedChains.toSet()).toList().distinct()
    val sessionMethods = requestedMethods.intersect(supportedMethods.toSet()).toList().distinct()

    if (sessionChains.isEmpty()) throw Exception("Unsupported chains")
    if (sessionMethods.isEmpty()) throw Exception("Unsupported methods")

    if (!sessionChains.all { chain -> CoreValidator.isChainIdCAIP2Compliant(chain) }) throw Exception("Chains are not CAIP-2 compliant")
    if (!sessionChains.all { chain -> SignValidator.getNamespaceKeyFromChainId(chain) == "eip155" }) throw Exception("Only eip155(EVM) is supported")

    val actionsJsonObject = JSONObject()
    val chainsJsonArray = JSONArray()
    sessionChains.forEach { chain -> chainsJsonArray.put(chain) }
    sessionMethods.forEach { method -> actionsJsonObject.put("request/$method", JSONArray().put(0, JSONObject().put("chains", chainsJsonArray))) }
    val recaps = JSONObject().put(Cacao.Payload.ATT_KEY, JSONObject().put("eip155", actionsJsonObject)).toString().replace("\\/", "/")

    val base64Recaps = Base64.toBase64String(recaps.toByteArray(Charsets.UTF_8))
    val newReCapsUrl = "${RECAPS_PREFIX}$base64Recaps"
    if (payloadParams.resources == null) {
        payloadParams.resources = listOf(newReCapsUrl)
    } else {
        val newResourcesList = payloadParams.resources!!
            .decodeReCaps()!!
            .filter { decoded -> !decoded.contains("eip155") }
            .map { reCaps -> "$RECAPS_PREFIX${Base64.toBase64String(reCaps.toByteArray(Charsets.UTF_8))}" }
            .plus(payloadParams.resources!!.filter { resource -> !resource.startsWith(RECAPS_PREFIX) })
            .plus(newReCapsUrl)

        payloadParams.resources = newResourcesList
    }

    return with(payloadParams) {
        Sign.Model.PayloadParams(
            chains = sessionChains,
            domain = domain,
            nonce = nonce,
            aud = aud,
            type = type,
            nbf = nbf,
            exp = exp,
            iat = iat,
            statement = statement,
            resources = resources,
            requestId = requestId
        )
    }
}