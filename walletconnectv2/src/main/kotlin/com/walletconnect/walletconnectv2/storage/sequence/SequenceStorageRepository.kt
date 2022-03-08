package com.walletconnect.walletconnectv2.storage.sequence

import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
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
        pairingDaoQueries.getListOfPairingDaos(mapper = this@SequenceStorageRepository::mapPairingDaoToPairingVO).executeAsList()

    @JvmSynthetic
    fun getListOfSessionVOs(): List<SessionVO> =
        sessionDaoQueries.getListOfSessionDaos(mapper = this@SequenceStorageRepository::mapSessionDaoToSessionVO).executeAsList()

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
        pairingDaoQueries.getPairingByTopic(topic.value).executeAsOne()
            .let { entity ->
                PairingVO(
                    topic = TopicVO(entity.topic),
                    status = entity.status,
                    expiry = ExpiryVO(entity.expiry),
                    uri = entity.uri,
                    relayProtocol = entity.relay_protocol,
                    relayData = entity.relay_data,
                    outcomeTopic = TopicVO(entity.outcome_topic ?: String.Empty)
                )
            }

    @JvmSynthetic
    fun getSessionByTopic(topic: TopicVO): SessionVO =
        sessionDaoQueries.getSessionByTopic(topic.value, mapper = this@SequenceStorageRepository::mapSessionDaoToSessionVO).executeAsOne()

    //insert: Proposed, Responded
    @JvmSynthetic
    fun insertPairing(pairing: PairingVO, settledTopic: TopicVO? = null) {
        val selfMetadataId = insertMetaData(pairing.selfMetaData)
        val peerMetadataId = insertMetaData(pairing.peerMetaData)

        with(pairing) {
            pairingDaoQueries.insertPendingPairing(
                topic.value,
                expiry.seconds,
                status,
                selfMetadataId,
                peerMetadataId,
                relayProtocol,
                relayData,
                uri,
                settledTopic?.value //todo:propably not needed, remove
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

//    @JvmSynthetic
//    fun updateAcknowledgedPairingMetadata(metaData: AppMetaDataVO, topic: TopicVO) {
//        val metadataId = insertMetaData(metaData)
//        pairingDaoQueries.updateAcknowledgedPairingMetadata(metadataId, topic.value)
//    }

    @JvmSynthetic
    fun deletePairing(topic: TopicVO) {
        metaDataDaoQueries.deleteSessionSelfMetaDataFromTopic(topic.value)
        metaDataDaoQueries.deletePairingSelfMetaDataFromTopic(topic.value)
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
                expiry = expiry.seconds,
                status = status,
                self_metadata_id = metadataId, //todo: peer or self metadata id?
                self_participant = selfParticipant.keyAsHex,
                relay_protocol = session.relayProtocol,
                relay_data = session.relayData,
                outcome_topic = outcomeTopic.value
            )
        }
    }

    //insert: Pre-Settled, Acknowledged
    @JvmSynthetic
    fun insertSettledSession(session: SessionVO, appMetaData: AppMetaDataVO?) {
        val selfMetadataId = insertMetaData(session.selfMetaData)
        val peerMetadataId = insertMetaData(session.peerMetaData)

        with(session) {
            sessionDaoQueries.insertSettleSession(
                topic = topic.value,
                permissions_chains = chains,
                permissions_methods = methods,
                permissions_types = types,
                expiry = expiry.seconds,
                status = status,
                //todo: peer or self metadata id?
                self_metadata_id = selfMetadataId,
                peer_metadata_id = peerMetadataId,
                self_participant = selfParticipant.keyAsHex,
                relay_protocol = session.relayProtocol,
                controller_key = session.controllerKey?.keyAsHex,
                peer_participant = session.peerParticipant?.keyAsHex,
                accounts = session.accounts,
                relay_data = session.relayData,
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
        metaDataDaoQueries.deleteSessionSelfMetaDataFromTopic(topic.value)
        metaDataDaoQueries.deleteSessionPeerMetaDataFromTopic(topic.value)
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
        status: SequenceStatus,
        relay_protocol: String,
        relay_data: String?,
        uri: String,
        selfName: String?,
        selfDesc: String?,
        selfUrl: String?,
        selfIcons: List<String>?,
        peerName: String?,
        peerDesc: String?,
        peerUrl: String?,
        peerIcons: List<String>?
    ): PairingVO {
        val selfMetaData = if (selfName != null && selfDesc != null && selfUrl != null && selfIcons != null) {
            AppMetaDataVO(selfName, selfDesc, selfUrl, selfIcons)
        } else {
            null
        }

        val peerMetaData = if (peerName != null && peerDesc != null && peerUrl != null && peerIcons != null) {
            AppMetaDataVO(peerName, peerDesc, peerUrl, peerIcons)
        } else {
            null
        }

        return PairingVO(
            topic = TopicVO(topic),
            expiry = ExpiryVO(expirySeconds),
            status = status,
            selfMetaData = selfMetaData,
            peerMetaData = peerMetaData,
            relayProtocol = relay_protocol,
            relayData = relay_data,
            uri = uri
        )
    }

    private fun mapSessionDaoToSessionVO(
        topic: String,
        expiry: Long,
        status: SequenceStatus,
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
        accounts: List<String>?,
        permission_chains: List<String>,
        permissions_methods: List<String>,
        permissions_types: List<String>?,
        outcome_topic: String?
    ): SessionVO {

        val selfMetaData = if (selfName != null && selfDesc != null && selfUrl != null && selfIcons != null) {
            AppMetaDataVO(selfName, selfDesc, selfUrl, selfIcons)
        } else {
            null
        }

        val peerMetaData = if (peerName != null && peerDesc != null && peerUrl != null && peerIcons != null) {
            AppMetaDataVO(peerName, peerDesc, peerUrl, peerIcons)
        } else {
            null
        }

        return SessionVO(
            topic = TopicVO(topic),
            chains = permission_chains,
            methods = permissions_methods,
            types = permissions_types,
            accounts = accounts ?: emptyList(),
            expiry = ExpiryVO(expiry),
            status = status,
            selfMetaData = selfMetaData,
            peerMetaData = peerMetaData,
            selfParticipant = PublicKey(self_participant),
            peerParticipant = PublicKey(peer_participant ?: String.Empty),
            controllerKey = PublicKey(controller_key ?: String.Empty),
            relayProtocol = relay_protocol,
            relayData = relay_data,
            outcomeTopic = TopicVO(outcome_topic ?: String.Empty)
        )
    }

    private companion object {
        const val FAILED_INSERT_ID = -1L
    }
}