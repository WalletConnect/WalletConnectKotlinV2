@file:JvmSynthetic

package com.walletconnect.sign.storage.sequence

import android.database.sqlite.SQLiteException
import com.walletconnect.sign.core.model.type.enums.MetaDataType
import com.walletconnect.sign.core.model.vo.ExpiryVO
import com.walletconnect.sign.core.model.vo.PublicKey
import com.walletconnect.sign.core.model.vo.TopicVO
import com.walletconnect.sign.core.model.vo.clientsync.common.MetaDataVO
import com.walletconnect.sign.core.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.core.model.vo.sequence.PairingVO
import com.walletconnect.sign.core.model.vo.sequence.SessionVO
import com.walletconnect.sign.storage.data.dao.metadata.MetaDataDaoQueries
import com.walletconnect.sign.storage.data.dao.namespace.NamespaceDaoQueries
import com.walletconnect.sign.storage.data.dao.namespace.NamespaceExtensionDaoQueries
import com.walletconnect.sign.storage.data.dao.pairing.PairingDaoQueries
import com.walletconnect.sign.storage.data.dao.proposalnamespace.ProposalNamespaceDaoQueries
import com.walletconnect.sign.storage.data.dao.proposalnamespace.ProposalNamespaceExtensionDaoQueries
import com.walletconnect.sign.storage.data.dao.session.SessionDaoQueries
import com.walletconnect.sign.storage.data.dao.temp.TempNamespaceDaoQueries
import com.walletconnect.sign.storage.data.dao.temp.TempNamespaceExtensionDaoQueries
import com.walletconnect.sign.util.Empty
import com.walletconnect.sign.util.isSequenceValid

//TODO: Split into SessionStorageRepository and PairingStorageRepository
internal class SequenceStorageRepository(
    private val pairingDaoQueries: PairingDaoQueries,
    private val sessionDaoQueries: SessionDaoQueries,
    private val metaDataDaoQueries: MetaDataDaoQueries,
    private val namespaceDaoQueries: NamespaceDaoQueries,
    private val extensionsDaoQueries: NamespaceExtensionDaoQueries,
    private val proposalNamespaceDaoQueries: ProposalNamespaceDaoQueries,
    private val proposalExtensionsDaoQueries: ProposalNamespaceExtensionDaoQueries,
    private val tempNamespaceDaoQueries: TempNamespaceDaoQueries,
    private val tempExtensionsDaoQueries: TempNamespaceExtensionDaoQueries,
) {

    @JvmSynthetic
    var onSequenceExpired: (topic: TopicVO) -> Unit = {}

    @JvmSynthetic
    fun getListOfPairingVOs(): List<PairingVO> =
        pairingDaoQueries.getListOfPairingDaos(mapper = this@SequenceStorageRepository::mapPairingDaoToPairingVO).executeAsList()

    @JvmSynthetic
    fun getListOfSessionVOs(): List<SessionVO> =
        sessionDaoQueries.getListOfSessionDaos(mapper = this@SequenceStorageRepository::mapSessionDaoToSessionVO).executeAsList()

    @JvmSynthetic
    fun isSessionValid(topic: TopicVO): Boolean {
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
    fun isPairingValid(topic: TopicVO): Boolean {
        val hasTopic = pairingDaoQueries.hasTopic(topic.value).executeAsOneOrNull() != null

        return if (hasTopic) {
            val expiry = pairingDaoQueries.getExpiry(topic.value).executeAsOne()
            verifyExpiry(expiry, topic) { pairingDaoQueries.deletePairing(topic.value) }
        } else {
            false
        }
    }

    @JvmSynthetic
    fun getPairingByTopic(topic: TopicVO): PairingVO =
        pairingDaoQueries.getPairingByTopic(topic.value).executeAsOne().let { entity ->
            PairingVO(
                topic = TopicVO(entity.topic),
                expiry = ExpiryVO(entity.expiry),
                uri = entity.uri,
                relayProtocol = entity.relay_protocol,
                relayData = entity.relay_data,
                isActive = entity.is_active
            )
        }

    @JvmSynthetic
    fun getSessionByTopic(topic: TopicVO): SessionVO =
        sessionDaoQueries.getSessionByTopic(topic.value, mapper = this@SequenceStorageRepository::mapSessionDaoToSessionVO).executeAsOne()

    @JvmSynthetic
    @Throws(SQLiteException::class)
    fun insertPairing(pairing: PairingVO) {
        with(pairing) {
            pairingDaoQueries.insertOrAbortPairing(
                topic.value,
                expiry.seconds,
                relayProtocol,
                relayData,
                uri,
                isActive
            )
        }
    }

    @JvmSynthetic
    fun activatePairing(topic: TopicVO, expiryInSeconds: Long) {
        pairingDaoQueries.activatePairing(expiryInSeconds, true, topic.value)
    }

    @JvmSynthetic
    @Throws(SQLiteException::class)
    fun upsertPairingPeerMetadata(topic: TopicVO, metaData: MetaDataVO) {
        if (metaDataDaoQueries.getByTopic(topic.value).executeAsOneOrNull() == null) {
            insertMetaData(metaData, MetaDataType.PEER, topic)
        } else {
            metaDataDaoQueries.updateOrAbortMetaData(
                metaData.name,
                metaData.description,
                metaData.url,
                metaData.icons,
                MetaDataType.PEER,
                topic.value
            )
        }
    }

    @JvmSynthetic
    fun deletePairing(topic: TopicVO) {
        metaDataDaoQueries.deleteMetaDataFromTopic(topic.value)
        pairingDaoQueries.deletePairing(topic.value)
    }

    @Synchronized
    @JvmSynthetic
    @Throws(SQLiteException::class)
    fun insertSession(session: SessionVO, requestId: Long) {
        with(session) {
            sessionDaoQueries.insertOrAbortSession(
                topic = topic.value,
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
        insertMetaData(session.selfMetaData, MetaDataType.SELF, session.topic)
        insertMetaData(session.peerMetaData, MetaDataType.PEER, session.topic)
        insertNamespace(session.namespaces, lastInsertedSessionId, requestId)
        insertProposalNamespace(session.proposalNamespaces, lastInsertedSessionId)
    }

    @JvmSynthetic
    fun acknowledgeSession(topic: TopicVO) {
        sessionDaoQueries.acknowledgeSession(true, topic.value)
    }

    @JvmSynthetic
    fun extendSession(topic: TopicVO, expiryInSeconds: Long) {
        sessionDaoQueries.updateSessionExpiry(expiryInSeconds, topic.value)
    }

    @JvmSynthetic
    fun insertUnAckNamespaces(
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
    fun getUnAckNamespaces(topic: String, requestId: Long): Map<String, NamespaceVO.Session> {
        return tempNamespaceDaoQueries.getTempNamespacesByRequestIdAndTopic(topic, requestId, mapper = ::mapTempNamespaceToNamespaceVO)
            .executeAsList().let { listOfMappedTempNamespaces ->
                val mapOfTempNamespace = listOfMappedTempNamespaces.associate { (key, namespace) ->
                    key to namespace
                }

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
    fun markUnAckNamespaceAcknowledged(topic: String, requestId: Long) {
        tempNamespaceDaoQueries.markNamespaceAcknowledged(topic, requestId)
    }

    @JvmSynthetic
    fun deleteSession(topic: TopicVO) {
        metaDataDaoQueries.deleteMetaDataFromTopic(topic.value)
        namespaceDaoQueries.deleteNamespacesByTopic(topic.value)
        extensionsDaoQueries.deleteNamespacesExtensionsByTopic(topic.value)
        proposalNamespaceDaoQueries.deleteProposalNamespacesByTopic(topic.value)
        proposalExtensionsDaoQueries.deleteProposalNamespacesExtensionsByTopic(topic.value)
        tempNamespaceDaoQueries.deleteTempNamespacesByTopic(topic.value)
        tempExtensionsDaoQueries.deleteTempNamespacesExtensionByTopic(topic.value)
        sessionDaoQueries.deleteSession(topic.value)
    }

    @Throws(SQLiteException::class)
    private fun insertMetaData(metaData: MetaDataVO?, metaDataType: MetaDataType, topic: TopicVO) {
        metaData?.let {
            metaDataDaoQueries.insertOrAbortMetaData(
                topic.value,
                metaData.name,
                metaData.description,
                metaData.url,
                metaData.icons,
                metaDataType
            )
        }
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

    private fun verifyExpiry(expiry: Long, topic: TopicVO, deleteSequence: () -> Unit): Boolean {
        return if (ExpiryVO(expiry).isSequenceValid()) {
            true
        } else {
            deleteSequence()
            onSequenceExpired(topic)
            false
        }
    }

    private fun mapPairingDaoToPairingVO(
        topic: String,
        expirySeconds: Long,
        relay_protocol: String,
        relay_data: String?,
        uri: String,
        peerName: String?,
        peerDesc: String?,
        peerUrl: String?,
        peerIcons: List<String>?,
        is_active: Boolean,
    ): PairingVO {
        val peerMetaData = if (peerName != null && peerDesc != null && peerUrl != null && peerIcons != null) {
            MetaDataVO(peerName, peerDesc, peerUrl, peerIcons)
        } else {
            null
        }

        return PairingVO(
            topic = TopicVO(topic),
            expiry = ExpiryVO(expirySeconds),
            peerMetaData = peerMetaData,
            relayProtocol = relay_protocol,
            relayData = relay_data,
            uri = uri,
            isActive = is_active
        )
    }

    private fun mapSessionDaoToSessionVO(
        id: Long,
        topic: String,
        expiry: Long,
        relay_protocol: String,
        relay_data: String?,
        controller_key: String?,
        self_participant: String,
        selfName: String?,
        selfDesc: String?,
        selfUrl: String?,
        selfIcons: List<String>?,
        peer_participant: String?,
        peerName: String?,
        peerDesc: String?,
        peerUrl: String?,
        peerIcons: List<String>?,
        is_acknowledged: Boolean,
    ): SessionVO {
        val selfMetaData = if (selfName != null && selfDesc != null && selfUrl != null && selfIcons != null) {
            MetaDataVO(selfName, selfDesc, selfUrl, selfIcons)
        } else {
            null
        }

        val peerMetaData = if (peerName != null && peerDesc != null && peerUrl != null && peerIcons != null) {
            MetaDataVO(peerName, peerDesc, peerUrl, peerIcons)
        } else {
            null
        }

        val sessionNamespaces: Map<String, NamespaceVO.Session> = getSessionNamespaces(id)
        val proposalNamespaces: Map<String, NamespaceVO.Proposal> = getProposalNamespaces(id)

        return SessionVO(
            topic = TopicVO(topic),
            expiry = ExpiryVO(expiry),
            selfMetaData = selfMetaData,
            peerMetaData = peerMetaData,
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