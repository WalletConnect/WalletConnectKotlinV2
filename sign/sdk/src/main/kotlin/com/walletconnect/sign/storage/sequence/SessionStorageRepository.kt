@file:JvmSynthetic

package com.walletconnect.sign.storage.sequence

import android.database.sqlite.SQLiteException
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.common.model.vo.sequence.SessionVO
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
        namespaces: Map<String, NamespaceVO.Session>,
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
    fun getTempNamespaces(requestId: Long): Map<String, NamespaceVO.Session> {
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
        namespaces: Map<String, NamespaceVO.Session>,
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
        namespaceDaoQueries.deleteNamespacesByTopic(topic.value)
        requiredNamespaceDaoQueries.deleteProposalNamespacesByTopic(topic.value)
        optionalNamespaceDaoQueries.deleteOptionalNamespacesByTopic(topic.value)
        tempNamespaceDaoQueries.deleteTempNamespacesByTopic(topic.value)
        sessionDaoQueries.deleteSession(topic.value)
    }

    @Throws(SQLiteException::class)
    private fun insertNamespace(namespaces: Map<String, NamespaceVO.Session>, sessionId: Long, requestId: Long) {
        namespaces.forEach { (key, value) ->
            namespaceDaoQueries.insertOrAbortNamespace(sessionId, key, value.chains, value.accounts, value.methods, value.events, requestId)
        }
    }

    @Throws(SQLiteException::class)
    private fun insertRequiredNamespace(namespaces: Map<String, NamespaceVO.Required>, sessionId: Long) {
        namespaces.forEach { (key, value) ->
            requiredNamespaceDaoQueries.insertOrAbortProposalNamespace(sessionId, key, value.chains, value.methods, value.events)
        }
    }

    @Throws(SQLiteException::class)
    private fun insertOptionalNamespace(namespaces: Map<String, NamespaceVO.Optional>?, sessionId: Long) {
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
        val sessionNamespaces: Map<String, NamespaceVO.Session> = getSessionNamespaces(id)
        val requiredNamespaces: Map<String, NamespaceVO.Required> = getRequiredNamespaces(id)
        val optionalNamespaces: Map<String, NamespaceVO.Optional> = getOptionalNamespaces(id)

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

    private fun getSessionNamespaces(id: Long): Map<String, NamespaceVO.Session> {
        return namespaceDaoQueries.getNamespaces(id) { key, chains, accounts, methods, events ->
            key to NamespaceVO.Session(chains, accounts, methods, events)
        }.executeAsList().toMap()
    }

    private fun mapTempNamespaceToNamespaceVO(
        sessionId: Long,
        key: String,
        chains: List<String>?,
        accounts: List<String>,
        methods: List<String>,
        events: List<String>
    ): Pair<String, NamespaceVO.Session> {
        return key to NamespaceVO.Session(chains, accounts, methods, events)
    }
}
