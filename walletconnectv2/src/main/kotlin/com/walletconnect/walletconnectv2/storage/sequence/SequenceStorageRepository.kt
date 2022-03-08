package com.walletconnect.walletconnectv2.storage.sequence

import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.TtlVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.AppMetaDataVO
import com.walletconnect.walletconnectv2.core.model.vo.sequence.PairingVO
import com.walletconnect.walletconnectv2.core.model.vo.sequence.SessionVO
import com.walletconnect.walletconnectv2.storage.data.dao.MetaDataDaoQueries
import com.walletconnect.walletconnectv2.storage.data.dao.PairingDaoQueries
import com.walletconnect.walletconnectv2.storage.data.dao.SessionDaoQueries
import com.walletconnect.walletconnectv2.util.Empty
import com.walletconnect.walletconnectv2.util.isSequenceValid

//TODO: Split into SessionStorageRepository and PairingStorageRepository
internal class SequenceStorageRepository(
    private val pairingDaoQueries: PairingDaoQueries,
    private val sessionDaoQueries: SessionDaoQueries,
    private val metaDataDaoQueries: MetaDataDaoQueries
) {

    @JvmSynthetic
    var onSequenceExpired: (topic: TopicVO) -> Unit = {}

    @JvmSynthetic
    fun getListOfPairingVOs(): List<PairingVO> =
        pairingDaoQueries.getListOfPairingDaos(mapper = this@SequenceStorageRepository::mapPairingDaoToPairingVO)
            .executeAsList()

    @JvmSynthetic
    fun getListOfSessionVOs(): List<SessionVO> =
        sessionDaoQueries.getListOfSessionDaos(mapper = this@SequenceStorageRepository::mapSessionDaoToSessionVO)
            .executeAsList()

    @JvmSynthetic
    fun isSessionValid(topic: TopicVO): Boolean {
        val hasTopic = sessionDaoQueries.hasTopic(topic.value).executeAsOneOrNull() != null

        if (hasTopic) {
            val expiry = sessionDaoQueries.getExpiry(topic.value).executeAsOne()
            return verifyExpiry(expiry, topic) { sessionDaoQueries.deleteSession(topic.value) }
        }
        return false
    }

    @JvmSynthetic
    fun isPairingValid(topic: TopicVO): Boolean {
        val hasTopic = pairingDaoQueries.hasTopic(topic.value).executeAsOneOrNull() != null

        if (hasTopic) {
            val expiry = pairingDaoQueries.getExpiry(topic.value).executeAsOne()
            return verifyExpiry(expiry, topic) { pairingDaoQueries.deletePairing(topic.value) }
        }
        return false
    }

    @JvmSynthetic
    fun getPairingByTopic(topic: TopicVO): PairingVO =
        pairingDaoQueries.getPairingByTopic(topic.value)
            .executeAsOne()
            .let { entity ->
                PairingVO(
                    topic = TopicVO(entity.topic),
                    status = entity.status,
                    expiry = ExpiryVO(entity.expiry),
                    selfParticipant = PublicKey(entity.self_participant),
                    peerParticipant = PublicKey(entity.peer_participant ?: String.Empty),
                    controllerKey = PublicKey(entity.controller_key ?: String.Empty),
                    uri = entity.uri,
                    permissions = entity.permissions,
                    relayProtocol = entity.relay_protocol,
                    relayData = entity.relay_data,
                    outcomeTopic = TopicVO(entity.outcome_topic ?: String.Empty)
                )
            }

    @JvmSynthetic
    fun getSessionByTopic(topic: TopicVO): SessionVO =
        sessionDaoQueries.getSessionByTopic(topic.value).executeAsOne().let { entity ->
            val appMetaData = if (entity._name != null && entity.description != null && entity.url != null && entity.icons != null) {
                AppMetaDataVO(entity._name, entity.description, entity.url, entity.icons)
            } else {
                null
            }

            SessionVO(
                topic = TopicVO(entity.topic),
                status = entity.status,
                expiry = ExpiryVO(entity.expiry),
                selfParticipant = PublicKey(entity.self_participant),
                peerParticipant = PublicKey(entity.peer_participant ?: String.Empty),
                controllerKey = PublicKey(entity.controller_key ?: String.Empty),
                chains = entity.permissions_chains,
                methods = entity.permissions_methods,
                types = entity.permissions_types,
                accounts = entity.accounts ?: emptyList(),
                ttl = TtlVO(entity.ttl_seconds),
                relayProtocol = entity.relay_protocol,
                outcomeTopic = TopicVO(entity.outcome_topic ?: String.Empty),
                metaData = appMetaData

            )
        }

    //insert: Proposed, Responded
    @JvmSynthetic
    fun insertPairing(pendingPairing: PairingVO, settledTopic: TopicVO? = null) {
        with(pendingPairing) {
            pairingDaoQueries.insertPendingPairing(
                topic.value,
                uri,
                expiry.seconds,
                status,
                selfParticipant.keyAsHex,
                relayProtocol,
                relayData,
                settledTopic?.value
            )
        }
    }

    @JvmSynthetic
    fun updatePairingExpiry(topic: TopicVO, expiryInSeconds: Long) {
        pairingDaoQueries.updatePairingExpiry(expiryInSeconds, topic.value)
    }

    @JvmSynthetic
    fun updatePreSettledPairingToAcknowledged(pairing: PairingVO) {
        pairingDaoQueries.updatePreSettledPairingToAcknowledged(pairing.status, pairing.topic.value)
    }

    @JvmSynthetic
    fun updateAcknowledgedPairingMetadata(metaData: AppMetaDataVO, topic: TopicVO) {
        val metadataId = insertMetaData(metaData)
        pairingDaoQueries.updateAcknowledgedPairingMetadata(metadataId, topic.value)
    }

    @JvmSynthetic
    fun deletePairing(topic: TopicVO) {
        metaDataDaoQueries.deleteMetaDataFromTopic(topic.value)
        pairingDaoQueries.deletePairing(topic.value)
    }

    //insert: Proposed, Responded
    @JvmSynthetic
    fun insertPendingSession(session: SessionVO, appMetaData: AppMetaDataVO?) {
        val metadataId = insertMetaData(appMetaData)

        with(session) {
            sessionDaoQueries.insertPendingSession(
                topic = topic.value,
                permissions_chains = chains,
                permissions_methods = methods,
                permissions_types = types,
                ttl_seconds = ttl.seconds,
                expiry = expiry.seconds,
                status = status,
                metadata_id = metadataId,
                self_participant = selfParticipant.keyAsHex,
                relay_protocol = session.relayProtocol,
                outcome_topic = outcomeTopic.value
            )
        }
    }

    //insert: Pre-Settled, Acknowledged
    @JvmSynthetic
    fun insertSettledSession(session: SessionVO, appMetaData: AppMetaDataVO?) {
        val metadataId = insertMetaData(appMetaData)

        with(session) {
            sessionDaoQueries.insertSettleSession(
                topic = topic.value,
                permissions_chains = chains,
                permissions_methods = methods,
                permissions_types = types,
                ttl_seconds = ttl.seconds,
                expiry = expiry.seconds,
                status = status,
                metadata_id = metadataId,
                self_participant = selfParticipant.keyAsHex,
                relay_protocol = session.relayProtocol,
                controller_key = session.controllerKey?.keyAsHex,
                peer_participant = session.peerParticipant?.keyAsHex,
                accounts = session.accounts
            )
        }
    }

    @JvmSynthetic
    fun updatePreSettledSessionToAcknowledged(session: SessionVO) {
        sessionDaoQueries.updatePreSettledSessionToAcknowledged(session.status, session.topic.value)
    }

    @JvmSynthetic
    fun updateSessionExpiry(topic: TopicVO, expiryInSeconds: Long) {
        sessionDaoQueries.updateSessionExpiry(expiryInSeconds, topic.value)
    }

    @JvmSynthetic
    fun updateSessionWithAccounts(topic: TopicVO, accounts: List<String>) {
        sessionDaoQueries.updateSessionWithAccounts(accounts, topic.value)
    }

    @JvmSynthetic
    fun upgradeSessionWithPermissions(topic: TopicVO, notificationTypes: List<String>?, jsonRpcMethods: List<String>?) {
        val (listOfTypes, listOfMethods) = sessionDaoQueries.getPermissionsByTopic(topic.value).executeAsOne()
        val typesUnion = listOfTypes?.union((notificationTypes ?: emptyList()))?.toList()
        val methodsUnion = listOfMethods.union((jsonRpcMethods ?: emptyList())).toList()
        sessionDaoQueries.updateSessionWithPermissions(typesUnion, methodsUnion, topic.value)
    }

    @JvmSynthetic
    fun deleteSession(topic: TopicVO) {
        metaDataDaoQueries.deleteMetaDataFromTopic(topic.value)
        sessionDaoQueries.deleteSession(topic.value)
    }

    private fun insertMetaData(appMetaData: AppMetaDataVO?): Long {
        return appMetaData?.let {
            metaDataDaoQueries.insertOrIgnoreMetaData(
                appMetaData.name,
                appMetaData.description,
                appMetaData.url,
                appMetaData.icons
            )

            metaDataDaoQueries.lastInsertedRowId().executeAsOne()
        } ?: FAILED_INSERT_ID
    }

    private fun verifyExpiry(expiry: Long, topic: TopicVO, deleteSequence: () -> Unit): Boolean {
        return if (ExpiryVO(expiry).isSequenceValid()) true else {
            deleteSequence()
            onSequenceExpired(topic)
            false
        }
    }

    private fun mapPairingDaoToPairingVO(
        topic: String,
        expirySeconds: Long,
        uri: String,
        status: SequenceStatus,
        self_participant: String,
        peer_participant: String?,
        controller_key: String?,
        relay_protocol: String,
        relay_data: String?,
        permissions: List<String>?,
        metadataName: String?,
        metadataDesc: String?,
        metadataUrl: String?,
        metadataIcons: List<String>?
    ): PairingVO {
        val appMetaData = if (metadataName != null && metadataDesc != null && metadataUrl != null && metadataIcons != null) {
            AppMetaDataVO(metadataName, metadataDesc, metadataUrl, metadataIcons)
        } else {
            null
        }

        return PairingVO(
            topic = TopicVO(topic),
            expiry = ExpiryVO(expirySeconds),
            status = status,
            selfParticipant = PublicKey(self_participant),
            peerParticipant = PublicKey(peer_participant ?: String.Empty),
            permissions = permissions,
            controllerKey = PublicKey(controller_key ?: String.Empty),
            uri = uri,
            relayProtocol = relay_protocol,
            relayData = relay_data,
            appMetaDataVO = appMetaData
        )
    }

    private fun mapSessionDaoToSessionVO(
        topic: String,
        permission_chains: List<String>,
        permissions_methods: List<String>,
        permissions_types: List<String>?,
        ttl_seconds: Long,
        accounts: List<String>?,
        expiry: Long,
        status: SequenceStatus,
        metadataName: String?,
        metadataDesc: String?,
        metadataUrl: String?,
        metadataIcons: List<String>?,
        self_participant: String,
        peer_participant: String?,
        controller_key: String?,
        relay_protocol: String
    ): SessionVO {
        val appMetaData = if (metadataName != null && metadataDesc != null && metadataUrl != null && metadataIcons != null) {
            AppMetaDataVO(metadataName, metadataDesc, metadataUrl, metadataIcons)
        } else {
            null
        }

        return SessionVO(
            topic = TopicVO(topic),
            chains = permission_chains,
            methods = permissions_methods,
            types = permissions_types,
            ttl = TtlVO(ttl_seconds),
            accounts = accounts ?: emptyList(),
            expiry = ExpiryVO(expiry),
            status = status,
            metaData = appMetaData,
            selfParticipant = PublicKey(self_participant),
            peerParticipant = PublicKey(peer_participant ?: String.Empty),
            controllerKey = PublicKey(controller_key ?: String.Empty),
            relayProtocol = relay_protocol
        )
    }

    private companion object {
        const val FAILED_INSERT_ID = -1L
    }
}