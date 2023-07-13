@file:JvmSynthetic

package com.walletconnect.sign.storage.proposal

import android.database.sqlite.SQLiteException
import com.walletconnect.android.internal.common.scope
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.common.model.vo.proposal.ProposalVO
import com.walletconnect.sign.storage.data.dao.optionalnamespaces.OptionalNamespaceDaoQueries
import com.walletconnect.sign.storage.data.dao.proposal.ProposalDaoQueries
import com.walletconnect.sign.storage.data.dao.proposalnamespace.ProposalNamespaceDaoQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProposalStorageRepository(
    private val proposalDaoQueries: ProposalDaoQueries,
    private val requiredNamespaceDaoQueries: ProposalNamespaceDaoQueries,
    private val optionalNamespaceDaoQueries: OptionalNamespaceDaoQueries
) {

    @JvmSynthetic
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
            properties,
            redirect
        )

        insertRequiredNamespace(requiredNamespaces, requestId)
        insertOptionalNamespace(optionalNamespaces, requestId)
    }

    @JvmSynthetic
    @Throws(SQLiteException::class)
    internal fun getProposalByKey(proposerPubKey: String): ProposalVO {
        return proposalDaoQueries.getProposalByKey(proposerPubKey, mapper = this::mapProposalDaoToProposalVO).executeAsOne()
    }

    @JvmSynthetic
    @Throws(SQLiteException::class)
    internal fun getProposals(): List<ProposalVO> {
        return proposalDaoQueries.getListOfProposalDaos(this::mapProposalDaoToProposalVO).executeAsList()
    }

    @JvmSynthetic
    internal fun deleteProposal(key: String) {
        scope.launch {
            withContext(Dispatchers.IO) {
                requiredNamespaceDaoQueries.deleteProposalNamespacesProposerKey(key)
                optionalNamespaceDaoQueries.deleteProposalNamespacesProposerKey(key)
                proposalDaoQueries.deleteProposal(key)
            }
        }
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
        properties: Map<String, String>?,
        redirect: String,
    ): ProposalVO {
        val requiredNamespaces: Map<String, NamespaceVO.Proposal> = getRequiredNamespaces(request_id)
        val optionalNamespaces: Map<String, NamespaceVO.Proposal> = getOptionalNamespaces(request_id)

        return ProposalVO(
            requestId = request_id,
            pairingTopic = Topic(pairingTopic),
            name = name,
            description = description,
            url = url,
            icons = icons,
            redirect = redirect,
            relayProtocol = relay_protocol,
            relayData = relay_data,
            proposerPublicKey = proposer_key,
            properties = properties,
            requiredNamespaces = requiredNamespaces,
            optionalNamespaces = optionalNamespaces
        )
    }

    @Throws(SQLiteException::class)
    private fun insertRequiredNamespace(namespaces: Map<String, NamespaceVO.Proposal>, proposalId: Long) {
        namespaces.forEach { (key, value) ->
            requiredNamespaceDaoQueries.insertOrAbortProposalNamespace(proposalId, key, value.chains, value.methods, value.events)
        }
    }

    @Throws(SQLiteException::class)
    private fun insertOptionalNamespace(namespaces: Map<String, NamespaceVO.Proposal>?, proposalId: Long) {
        namespaces?.forEach { (key, value) ->
            optionalNamespaceDaoQueries.insertOrAbortOptionalNamespace(proposalId, key, value.chains, value.methods, value.events)
        }
    }

    private fun getRequiredNamespaces(id: Long): Map<String, NamespaceVO.Proposal> {
        return requiredNamespaceDaoQueries.getProposalNamespaces(id) { key, chains, methods, events ->
            key to NamespaceVO.Proposal(chains = chains, methods = methods, events = events)
        }.executeAsList().toMap()
    }

    private fun getOptionalNamespaces(id: Long): Map<String, NamespaceVO.Proposal> {
        return optionalNamespaceDaoQueries.getOptionalNamespaces(id) { key, chains, methods, events ->
            key to NamespaceVO.Proposal(chains = chains, methods = methods, events = events)
        }.executeAsList().toMap()
    }
}