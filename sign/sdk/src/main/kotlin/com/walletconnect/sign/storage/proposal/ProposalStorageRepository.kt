@file:JvmSynthetic

package com.walletconnect.sign.storage.proposal

import android.database.sqlite.SQLiteException
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.common.model.vo.proposal.ProposalVO
import com.walletconnect.sign.common.model.vo.sequence.SessionVO
import com.walletconnect.sign.storage.data.dao.optionalnamespaces.OptionalNamespaceDaoQueries
import com.walletconnect.sign.storage.data.dao.proposal.ProposalDaoQueries
import com.walletconnect.sign.storage.data.dao.proposalnamespace.ProposalNamespaceDaoQueries
import com.walletconnect.utils.Empty

class ProposalStorageRepository(
    private val proposalDaoQueries: ProposalDaoQueries,
    private val requiredNamespaceDaoQueries: ProposalNamespaceDaoQueries,
    private val optionalNamespaceDaoQueries: OptionalNamespaceDaoQueries
) {

    @Throws(SQLiteException::class)
    internal fun insertProposal(proposal: ProposalVO) = with(proposal) {
        proposalDaoQueries.insertOrAbortSession(
            requestId,
            pairingTopic.value,
            name,
            description,
            url,
            icons,
            relayProtocol,
            relayData,
            proposerPublicKey,
            properties
        )

        insertRequiredNamespace(requiredNamespaces, requestId)
        insertOptionalNamespace(optionalNamespaces, requestId)
    }

    @Throws(SQLiteException::class)
    internal fun getProposalByKey(proposerPubKey: String): ProposalVO {
        return proposalDaoQueries.getProposalByKey(proposerPubKey, mapper = this::mapProposalDaoToProposalVO).executeAsOne()
    }

    @Throws(SQLiteException::class)
    internal fun getProposals(): List<ProposalVO> {
        return proposalDaoQueries.getListOfProposalDaos(this::mapProposalDaoToProposalVO).executeAsList()
    }

    @JvmSynthetic
    fun deleteProposal(key: String) {
        requiredNamespaceDaoQueries.deleteProposalNamespacesByTopic(key)
        optionalNamespaceDaoQueries.deleteOptionalNamespacesByTopic(key)
        proposalDaoQueries.deleteProposal(key)
    }

    private fun mapProposalDaoToProposalVO(
        request_id: Long,
        pairingTopic: String,
        name: String,
        description: String,
        url: String,
        icons: List<String>,
        relay_protocol: String,
        relay_data: String?,
        proposer_key: String,
        properties: Map<String, String>?
    ): ProposalVO {
        val requiredNamespaces: Map<String, NamespaceVO.Required> = getRequiredNamespaces(request_id)
        val optionalNamespaces: Map<String, NamespaceVO.Optional> = getOptionalNamespaces(request_id)

        return ProposalVO(
            requestId = request_id,
            pairingTopic = Topic(pairingTopic),
            name = name,
            description = description,
            url = url,
            icons = icons,
            relayProtocol = relay_protocol,
            relayData = relay_data,
            proposerPublicKey = proposer_key,
            properties = properties,
            requiredNamespaces = requiredNamespaces,
            optionalNamespaces = optionalNamespaces
        )
    }

    @Throws(SQLiteException::class)
    private fun insertRequiredNamespace(namespaces: Map<String, NamespaceVO.Required>, proposalId: Long) {
        namespaces.forEach { (key, value) ->
            requiredNamespaceDaoQueries.insertOrAbortProposalNamespace(proposalId, key, value.chains, value.methods, value.events)
        }
    }

    @Throws(SQLiteException::class)
    private fun insertOptionalNamespace(namespaces: Map<String, NamespaceVO.Optional>?, proposalId: Long) {
        namespaces?.forEach { (key, value) ->
            optionalNamespaceDaoQueries.insertOrAbortOptionalNamespace(proposalId, key, value.chains, value.methods, value.events)
        }
    }

    private fun getRequiredNamespaces(id: Long): Map<String, NamespaceVO.Required> {
        return requiredNamespaceDaoQueries.getProposalNamespaces(id) { key, chains, methods, events ->
            key to NamespaceVO.Required(chains, methods, events)
        }.executeAsList().toMap()
    }

    private fun getOptionalNamespaces(id: Long): Map<String, NamespaceVO.Optional> {
        return optionalNamespaceDaoQueries.getOptionalNamespaces(id) { key, chains, methods, events ->
            key to NamespaceVO.Optional(chains, methods, events)
        }.executeAsList().toMap()
    }
}