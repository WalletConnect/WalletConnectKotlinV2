@file:JvmSynthetic

package com.walletconnect.sign.storage.sequence

import android.database.sqlite.SQLiteException
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.common.model.vo.sequence.SessionVO
import com.walletconnect.sign.storage.data.dao.namespace.NamespaceDaoQueries
import com.walletconnect.sign.storage.data.dao.namespace.NamespaceExtensionDaoQueries
import com.walletconnect.sign.storage.data.dao.proposalnamespace.ProposalNamespaceDaoQueries
import com.walletconnect.sign.storage.data.dao.proposalnamespace.ProposalNamespaceExtensionDaoQueries
import com.walletconnect.sign.storage.data.dao.session.SessionDaoQueries
import com.walletconnect.sign.storage.data.dao.temp.TempNamespaceDaoQueries
import com.walletconnect.sign.storage.data.dao.temp.TempNamespaceExtensionDaoQueries
import com.walletconnect.utils.Empty
import com.walletconnect.utils.isSequenceValid

internal class SessionStorageRepository(
    private val sessionDaoQueries: SessionDaoQueries,
    private val namespaceDaoQueries: NamespaceDaoQueries,
    private val extensionsDaoQueries: NamespaceExtensionDaoQueries,
    private val proposalNamespaceDaoQueries: ProposalNamespaceDaoQueries,
    private val proposalExtensionsDaoQueries: ProposalNamespaceExtensionDaoQueries,
    private val tempNamespaceDaoQueries: TempNamespaceDaoQueries,
    private val tempExtensionsDaoQueries: TempNamespaceExtensionDaoQueries,
) {
    @JvmSynthetic
    var onSequenceExpired: (topic: Topic) -> Unit = {}

    @JvmSynthetic
    fun getListOfSessionVOsWithoutMetadata(): List<SessionVO> =
        sessionDaoQueries.getListOfSessionDaos(mapper = this@SessionStorageRepository::mapSessionDaoToSessionVO).executeAsList()

    @JvmSynthetic
    fun isSessionValid(topic: Topic): Boolean {
        val hasTopic = sessionDaoQueries.hasTopic(topic.value).executeAsOneOrNull() != null

        return if (hasTopic) {
            sessionDaoQueries.getExpiry(topic.value).executeAsOneOrNull()?.let { sessionExpiry ->
                verifyExpiry(sessionExpiry, topic) {
                    sessionDaoQueries.deleteSession(topic.value)
                }
            } ?: false
        } else {
            false
        }
    }

    @JvmSynthetic
    fun getSessionWithoutMetadataByTopic(topic: Topic): SessionVO =
        sessionDaoQueries.getSessionByTopic(topic.value, mapper = this@SessionStorageRepository::mapSessionDaoToSessionVO).executeAsOne()

    @JvmSynthetic
    fun getAllSessionTopicsByPairingTopic(pairingTopic: Topic): List<String> =
        sessionDaoQueries.getAllSessionTopicsByPairingTopic(pairingTopic.value).executeAsList()

    @Synchronized
    @JvmSynthetic
    @Throws(SQLiteException::class)
    fun insertSession(session: SessionVO, pairingTopic: Topic, requestId: Long) {
        with(session) {
            sessionDaoQueries.insertOrAbortSession(
                topic = topic.value,
                pairingTopic = pairingTopic.value,
                expiry = expiry.seconds,
                self_participant = selfPublicKey.keyAsHex,
                relay_protocol = relayProtocol,
                controller_key = controllerKey?.keyAsHex,
                peer_participant = peerPublicKey?.keyAsHex,
                relay_data = relayData,
                is_acknowledged = isAcknowledged
            )
        }

        val lastInsertedSessionId = sessionDaoQueries.lastInsertedRow().executeAsOne()
        insertNamespace(session.namespaces, lastInsertedSessionId, requestId)
        insertProposalNamespace(session.proposalNamespaces, lastInsertedSessionId)
    }

    @JvmSynthetic
    fun acknowledgeSession(topic: Topic) {
        sessionDaoQueries.acknowledgeSession(true, topic.value)
    }

    @JvmSynthetic
    fun extendSession(topic: Topic, expiryInSeconds: Long) {
        sessionDaoQueries.updateSessionExpiry(expiryInSeconds, topic.value)
    }

    @JvmSynthetic
    fun insertTempNamespaces(
        topic: String,
        namespaces: Map<String, NamespaceVO.Session>,
        requestId: Long,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
    ) {
        val sessionId = sessionDaoQueries.getSessionIdByTopic(topic).executeAsOne()

        tempNamespaceDaoQueries.transaction namespace@{
            afterRollback { onFailure() }

            namespaces.forEach { key, (accounts: List<String>, methods: List<String>, events: List<String>, _: List<NamespaceVO.Session.Extension>?) ->
                tempNamespaceDaoQueries.insertOrAbortNamespace(sessionId, topic, key, accounts, methods, events, requestId)
            }

            tempExtensionsDaoQueries.transaction {
                afterRollback { this@namespace.rollback() }

                namespaces.mapValues { (_, namespace) -> namespace.extensions }.forEach { (key, listOfExtensions) ->
                    listOfExtensions?.forEach { extension ->
                        tempExtensionsDaoQueries.insertOrAbortNamespaceExtension(
                            key,
                            sessionId,
                            topic,
                            extension.accounts,
                            extension.methods,
                            extension.events,
                            requestId
                        )
                    }
                }

                afterCommit { onSuccess() }
            }
        }
    }

    @JvmSynthetic
    fun getTempNamespaces(requestId: Long): Map<String, NamespaceVO.Session> {
        return tempNamespaceDaoQueries.getTempNamespacesByRequestId(requestId, mapper = ::mapTempNamespaceToNamespaceVO)
            .executeAsList().let { listOfMappedTempNamespaces ->
                val mapOfTempNamespace = listOfMappedTempNamespaces.associate { (key, namespace) -> key to namespace }
                mapOfTempNamespace
            }
    }

    @JvmSynthetic
    fun deleteNamespaceAndInsertNewNamespace(
        topic: String,
        namespaces: Map<String, NamespaceVO.Session>,
        requestID: Long,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
    ) {
        val sessionId = sessionDaoQueries.getSessionIdByTopic(topic).executeAsOne()

        namespaceDaoQueries.transaction namespace@{
            afterRollback { onFailure() }

            namespaceDaoQueries.deleteNamespacesByTopic(topic)
            namespaces.forEach { key, (accounts: List<String>, methods: List<String>, events: List<String>, _: List<NamespaceVO.Session.Extension>?) ->
                namespaceDaoQueries.insertOrAbortNamespace(sessionId, key, accounts, methods, events, requestID)
            }

            extensionsDaoQueries.transaction {
                afterRollback { this@namespace.rollback() }

                extensionsDaoQueries.deleteNamespacesExtensionsByTopic(topic)
                namespaces.mapValues { (_, namespace) -> namespace.extensions }.forEach { (key, listOfExtensions) ->
                    listOfExtensions?.forEach { extension ->
                        extensionsDaoQueries.insertOrAbortNamespaceExtension(
                            key,
                            sessionId,
                            extension.accounts,
                            extension.methods,
                            extension.events
                        )
                    }
                }

                afterCommit { onSuccess() }
            }
        }
    }

    @JvmSynthetic
    fun isUpdatedNamespaceValid(topic: String, timestamp: Long): Boolean {
        return namespaceDaoQueries.isUpdateNamespaceRequestValid(timestamp, topic).executeAsOneOrNull() ?: false
    }

    @JvmSynthetic
    fun isUpdatedNamespaceResponseValid(topic: String, timestamp: Long): Boolean {
        return tempNamespaceDaoQueries.isUpdateNamespaceRequestValid(topic, timestamp).executeAsOneOrNull() ?: false
    }

    @JvmSynthetic
    fun markUnAckNamespaceAcknowledged(requestId: Long) {
        tempNamespaceDaoQueries.markNamespaceAcknowledged(requestId)
    }

    @JvmSynthetic
    fun deleteTempNamespacesByRequestId(requestId: Long) {
        tempNamespaceDaoQueries.deleteTempNamespacesByRequestId(requestId)
    }

    @JvmSynthetic
    fun deleteSession(topic: Topic) {
        namespaceDaoQueries.deleteNamespacesByTopic(topic.value)
        extensionsDaoQueries.deleteNamespacesExtensionsByTopic(topic.value)
        proposalNamespaceDaoQueries.deleteProposalNamespacesByTopic(topic.value)
        proposalExtensionsDaoQueries.deleteProposalNamespacesExtensionsByTopic(topic.value)
        tempNamespaceDaoQueries.deleteTempNamespacesByTopic(topic.value)
        tempExtensionsDaoQueries.deleteTempNamespacesExtensionByTopic(topic.value)
        sessionDaoQueries.deleteSession(topic.value)
    }

    @Throws(SQLiteException::class)
    private fun insertNamespace(namespaces: Map<String, NamespaceVO.Session>, sessionId: Long, requestId: Long) {
        namespaces.forEach { key, (accounts: List<String>, methods: List<String>, events: List<String>, extensions: List<NamespaceVO.Session.Extension>?) ->
            namespaceDaoQueries.insertOrAbortNamespace(sessionId, key, accounts, methods, events, requestId)

            extensions?.forEach { extension ->
                extensionsDaoQueries.insertOrAbortNamespaceExtension(
                    key,
                    sessionId,
                    extension.accounts,
                    extension.methods,
                    extension.events
                )
            }
        }
    }

    @Throws(SQLiteException::class)
    private fun insertProposalNamespace(namespaces: Map<String, NamespaceVO.Proposal>, sessionId: Long) {
        namespaces.forEach { key, (chains: List<String>, methods: List<String>, events: List<String>, extensions: List<NamespaceVO.Proposal.Extension>?) ->

            proposalNamespaceDaoQueries.insertOrAbortProposalNamespace(sessionId, key, chains, methods, events)

            extensions?.forEach { extension ->
                proposalExtensionsDaoQueries.insertOrAbortProposalNamespaceExtension(
                    key,
                    sessionId,
                    extension.chains,
                    extension.methods,
                    extension.events
                )
            }
        }
    }

    private fun verifyExpiry(expiry: Long, topic: Topic, deleteSequence: () -> Unit): Boolean {
        return if (Expiry(expiry).isSequenceValid()) {
            true
        } else {
            deleteSequence()
            onSequenceExpired(topic)
            false
        }
    }

    private fun mapSessionDaoToSessionVO(
        id: Long,
        topic: String,
        expiry: Long,
        relay_protocol: String,
        relay_data: String?,
        controller_key: String?,
        self_participant: String,
        peer_participant: String?,
        is_acknowledged: Boolean,
    ): SessionVO {
        val sessionNamespaces: Map<String, NamespaceVO.Session> = getSessionNamespaces(id)
        val proposalNamespaces: Map<String, NamespaceVO.Proposal> = getProposalNamespaces(id)

        return SessionVO(
            topic = Topic(topic),
            expiry = Expiry(expiry),
            selfAppMetaData = null,
            peerAppMetaData = null,
            selfPublicKey = PublicKey(self_participant),
            peerPublicKey = PublicKey(peer_participant ?: String.Empty),
            controllerKey = PublicKey(controller_key ?: String.Empty),
            relayProtocol = relay_protocol,
            relayData = relay_data,
            namespaces = sessionNamespaces,
            proposalNamespaces = proposalNamespaces,
            isAcknowledged = is_acknowledged
        )
    }

    private fun getProposalNamespaces(id: Long): Map<String, NamespaceVO.Proposal> {
        return proposalNamespaceDaoQueries.getProposalNamespaces(id) { key, chains, methods, events ->

            val extensions: List<NamespaceVO.Proposal.Extension>? =
                proposalExtensionsDaoQueries.getProposalNamespaceExtensionByKeyAndSessionId(key, id)
                { extAccounts, extMethods, extEvents -> NamespaceVO.Proposal.Extension(extAccounts, extMethods, extEvents) }
                    .executeAsList()
                    .takeIf { listOfNamespaceExtensions -> listOfNamespaceExtensions.isNotEmpty() }

            key to NamespaceVO.Proposal(chains, methods, events, extensions)

        }.executeAsList().toMap()
    }

    private fun getSessionNamespaces(id: Long): Map<String, NamespaceVO.Session> {
        return namespaceDaoQueries.getNamespaces(id) { key, accounts, methods, events ->
            val extensions: List<NamespaceVO.Session.Extension>? =
                extensionsDaoQueries.getNamespaceExtensionByNamespaceKeyAndSessionId(key, id)
                { extAccounts, extMethods, extEvents -> NamespaceVO.Session.Extension(extAccounts, extMethods, extEvents) }
                    .executeAsList()
                    .takeIf { listOfNamespaceExtensions -> listOfNamespaceExtensions.isNotEmpty() }

            key to NamespaceVO.Session(accounts, methods, events, extensions)

        }.executeAsList().toMap()
    }

    private fun mapTempNamespaceToNamespaceVO(
        sessionId: Long,
        key: String,
        accounts: List<String>,
        methods: List<String>,
        events: List<String>,
    ): Pair<String, NamespaceVO.Session> {
        val extensions = tempExtensionsDaoQueries.getNamespaceExtensionByNamespaceKeyAndSessionId(key, sessionId,
            mapper = ::mapTempNamespaceExtensionToNamespaceExtensionVO).executeAsList().takeIf { extensions -> extensions.isNotEmpty() }

        return key to NamespaceVO.Session(accounts, methods, events, extensions)
    }

    private fun mapTempNamespaceExtensionToNamespaceExtensionVO(
        accounts: List<String>,
        methods: List<String>,
        events: List<String>,
    ): NamespaceVO.Session.Extension {
        return NamespaceVO.Session.Extension(accounts, methods, events)
    }
}
