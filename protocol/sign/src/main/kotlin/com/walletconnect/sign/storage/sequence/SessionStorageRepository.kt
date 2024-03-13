@file:JvmSynthetic

package com.walletconnect.sign.storage.sequence

import android.database.sqlite.SQLiteException
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.Namespace
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.model.vo.sequence.SessionVO
import com.walletconnect.sign.engine.sessionRequestEventsQueue
import com.walletconnect.sign.storage.data.dao.namespace.NamespaceDaoQueries
import com.walletconnect.sign.storage.data.dao.optionalnamespaces.OptionalNamespaceDaoQueries
import com.walletconnect.sign.storage.data.dao.proposalnamespace.ProposalNamespaceDaoQueries
import com.walletconnect.sign.storage.data.dao.session.SessionDaoQueries
import com.walletconnect.sign.storage.data.dao.temp.TempNamespaceDaoQueries
import com.walletconnect.utils.Empty
import com.walletconnect.utils.isSequenceValid

internal class SessionStorageRepository(
    private val sessionDaoQueries: SessionDaoQueries,
    private val namespaceDaoQueries: NamespaceDaoQueries,
    private val requiredNamespaceDaoQueries: ProposalNamespaceDaoQueries,
    private val optionalNamespaceDaoQueries: OptionalNamespaceDaoQueries,
    private val tempNamespaceDaoQueries: TempNamespaceDaoQueries
) {
    @JvmSynthetic
    var onSessionExpired: (topic: Topic) -> Unit = {}

    @JvmSynthetic
    fun getListOfSessionVOsWithoutMetadata(): List<SessionVO> =
        sessionDaoQueries.getListOfSessionDaos(mapper = this@SessionStorageRepository::mapSessionDaoToSessionVO).executeAsList()

    // TODO: Maybe move this out and into SignValidator?
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
    fun getSessionExpiryByTopic(topic: Topic): Expiry? {
        return sessionDaoQueries.getExpiry(topic.value).executeAsOneOrNull()?.let { expiryLong ->
            Expiry(expiryLong)
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
    fun insertSession(session: SessionVO, requestId: Long) {
        with(session) {
            sessionDaoQueries.insertOrAbortSession(
                topic = topic.value,
                pairingTopic = pairingTopic,
                expiry = expiry.seconds,
                self_participant = selfPublicKey.keyAsHex,
                relay_protocol = relayProtocol,
                controller_key = controllerKey?.keyAsHex,
                peer_participant = peerPublicKey?.keyAsHex,
                relay_data = relayData,
                is_acknowledged = isAcknowledged,
                properties = properties
            )
        }

        val lastInsertedSessionId = sessionDaoQueries.lastInsertedRow().executeAsOne()
        insertNamespace(session.sessionNamespaces, lastInsertedSessionId, requestId)
        insertRequiredNamespace(session.requiredNamespaces, lastInsertedSessionId)
        insertOptionalNamespace(session.optionalNamespaces, lastInsertedSessionId)
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
    @Throws(SQLiteException::class)
    fun insertTempNamespaces(
        topic: String,
        namespaces: Map<String, Namespace.Session>,
        requestId: Long
    ) {
        val sessionId = sessionDaoQueries.getSessionIdByTopic(topic).executeAsOne()
        namespaces.forEach { (key, value) ->
            tempNamespaceDaoQueries.insertOrAbortNamespace(
                sessionId,
                topic,
                key,
                value.chains,
                value.accounts,
                value.methods,
                value.events,
                requestId
            )
        }
    }

    @JvmSynthetic
    fun getTempNamespaces(requestId: Long): Map<String, Namespace.Session> {
        return tempNamespaceDaoQueries.getTempNamespacesByRequestId(requestId, mapper = ::mapTempNamespaceToNamespaceVO)
            .executeAsList().let { listOfMappedTempNamespaces ->
                val mapOfTempNamespace = listOfMappedTempNamespaces.associate { (key, namespace) -> key to namespace }
                mapOfTempNamespace
            }
    }

    @JvmSynthetic
    @Throws(SQLiteException::class)
    fun deleteNamespaceAndInsertNewNamespace(
        topic: String,
        namespaces: Map<String, Namespace.Session>,
        requestID: Long
    ) {
        val sessionId = sessionDaoQueries.getSessionIdByTopic(topic).executeAsOne()
        namespaceDaoQueries.deleteNamespacesByTopic(topic)
        namespaces.forEach { (key, value) ->
            namespaceDaoQueries.insertOrAbortNamespace(sessionId, key, value.chains, value.accounts, value.methods, value.events, requestID)
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
        sessionRequestEventsQueue.removeAll { event -> event.request.topic == topic.value }
        namespaceDaoQueries.deleteNamespacesByTopic(topic.value)
        requiredNamespaceDaoQueries.deleteProposalNamespacesByTopic(topic.value)
        optionalNamespaceDaoQueries.deleteOptionalNamespacesByTopic(topic.value)
        tempNamespaceDaoQueries.deleteTempNamespacesByTopic(topic.value)
        sessionDaoQueries.deleteSession(topic.value)
    }

    @Throws(SQLiteException::class)
    private fun insertNamespace(namespaces: Map<String, Namespace.Session>, sessionId: Long, requestId: Long) {
        namespaces.forEach { (key, value) ->
            namespaceDaoQueries.insertOrAbortNamespace(sessionId, key, value.chains, value.accounts, value.methods, value.events, requestId)
        }
    }

    @Throws(SQLiteException::class)
    private fun insertRequiredNamespace(namespaces: Map<String, Namespace.Proposal>, sessionId: Long) {
        namespaces.forEach { (key, value) ->
            requiredNamespaceDaoQueries.insertOrAbortProposalNamespace(sessionId, key, value.chains, value.methods, value.events)
        }
    }

    @Throws(SQLiteException::class)
    private fun insertOptionalNamespace(namespaces: Map<String, Namespace.Proposal>?, sessionId: Long) {
        namespaces?.forEach { (key, value) ->
            optionalNamespaceDaoQueries.insertOrAbortOptionalNamespace(sessionId, key, value.chains, value.methods, value.events)
        }
    }

    private fun verifyExpiry(expiry: Long, topic: Topic, deleteSequence: () -> Unit): Boolean {
        return if (Expiry(expiry).isSequenceValid()) {
            true
        } else {
            deleteSequence()
            onSessionExpired(topic)
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
        pairingTopic: String,
        properties: Map<String, String>?
    ): SessionVO {
        val sessionNamespaces: Map<String, Namespace.Session> = getSessionNamespaces(id)
        val requiredNamespaces: Map<String, Namespace.Proposal> = getRequiredNamespaces(id)
        val optionalNamespaces: Map<String, Namespace.Proposal> = getOptionalNamespaces(id)

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
            sessionNamespaces = sessionNamespaces,
            requiredNamespaces = requiredNamespaces,
            optionalNamespaces = optionalNamespaces,
            isAcknowledged = is_acknowledged,
            properties = properties,
            pairingTopic = pairingTopic
        )
    }

    private fun getRequiredNamespaces(id: Long): Map<String, Namespace.Proposal> {
        return requiredNamespaceDaoQueries.getProposalNamespaces(id) { key, chains, methods, events ->
            key to Namespace.Proposal(chains = chains, methods = methods, events = events)
        }.executeAsList().toMap()
    }

    private fun getOptionalNamespaces(id: Long): Map<String, Namespace.Proposal> {
        return optionalNamespaceDaoQueries.getOptionalNamespaces(id) { key, chains, methods, events ->
            key to Namespace.Proposal(chains = chains, methods = methods, events = events)
        }.executeAsList().toMap()
    }

    private fun getSessionNamespaces(id: Long): Map<String, Namespace.Session> {
        return namespaceDaoQueries.getNamespaces(id) { key, chains, accounts, methods, events ->
            key to Namespace.Session(chains, accounts, methods, events)
        }.executeAsList().toMap()
    }

    private fun mapTempNamespaceToNamespaceVO(
        sessionId: Long,
        key: String,
        chains: List<String>?,
        accounts: List<String>,
        methods: List<String>,
        events: List<String>
    ): Pair<String, Namespace.Session> {
        return key to Namespace.Session(chains, accounts, methods, events)
    }
}
