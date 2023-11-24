package com.walletconnect.sign.common.model.vo.proposal

import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Redirect
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.android.internal.common.model.Namespace

internal data class ProposalVO(
    val requestId: Long,
    val pairingTopic: Topic,
    val name: String,
    val description: String,
    val url: String,
    val icons: List<String>,
    val redirect: String,
    val requiredNamespaces: Map<String, Namespace.Proposal>,
    val optionalNamespaces: Map<String, Namespace.Proposal>,
    val properties: Map<String, String>?,
    val proposerPublicKey: String,
    val relayProtocol: String,
    val relayData: String?,
) {
    val appMetaData: AppMetaData
        get() = AppMetaData(name = name, description = description, url = url, icons = icons, redirect = Redirect(native = redirect))
}