package com.walletconnect.sign.common.model.vo.proposal

import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO

internal data class ProposalVO(
    val requestId: Long,
    val pairingTopic: Topic,
    val name: String,
    val description: String,
    val url: String,
    val icons: List<String>,
    val requiredNamespaces: Map<String, NamespaceVO.Proposal>,
    val optionalNamespaces: Map<String, NamespaceVO.Proposal>,
    val properties: Map<String, String>?,
    val proposerPublicKey: String,
    val relayProtocol: String,
    val relayData: String?,
) {
    val appMetaData: AppMetaData
        get() = AppMetaData(name, description, url, icons)
}