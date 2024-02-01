package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.model.Namespace
import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.sign.common.validator.SignValidator

class GetNamespacesFromReCaps : GetNamespacesFromReCapsInterface {
    override fun getNamespacesFromReCaps(chains: List<String>, methods: List<String>): Map<String, Namespace.Proposal> {
        if (!chains.all { chain -> CoreValidator.isChainIdCAIP2Compliant(chain) }) throw Exception("Chains are not CAIP-2 compliant")
        if (chains.any { chain -> chain != "eip155" }) throw Exception("Only eip155 is supported")
        val namespace = SignValidator.getNamespaceKeyFromChainId(chains.first())
        return mapOf(namespace to Namespace.Proposal(events = listOf(), methods = methods, chains = chains))
    }
}

internal interface GetNamespacesFromReCapsInterface {
    fun getNamespacesFromReCaps(chains: List<String>, methods: List<String>): Map<String, Namespace.Proposal>
}