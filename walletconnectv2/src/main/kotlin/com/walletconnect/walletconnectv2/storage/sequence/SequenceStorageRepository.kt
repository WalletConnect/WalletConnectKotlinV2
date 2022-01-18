package com.walletconnect.walletconnectv2.storage.sequence

import com.walletconnect.walletconnectv2.Database
import com.walletconnect.walletconnectv2.common.model.type.ControllerType
import com.walletconnect.walletconnectv2.common.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.common.model.vo.TopicVO
import com.walletconnect.walletconnectv2.common.model.vo.TtlVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.SessionParamsVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.proposal.AppMetaDataVO
import com.walletconnect.walletconnectv2.common.model.vo.sequence.PairingVO
import com.walletconnect.walletconnectv2.common.model.vo.sequence.SessionVO

//TODO: Split into SessionStorageRepository and PairingStorageRepository
internal class SequenceStorageRepository constructor(private val sessionDatabase: Database) {

    @JvmSynthetic
    fun getListOfPairingVOs() =
        sessionDatabase.pairingDaoQueries.getListOfPairingDaos(mapper = this@SequenceStorageRepository::mapPairingDaoToPairingVO).executeAsList()

    @JvmSynthetic
    fun getListOfSessionVOs() =
        sessionDatabase.sessionDaoQueries.getListOfSessionDaos(mapper = this@SequenceStorageRepository::mapSessionDaoToSessionVO).executeAsList()

    @JvmSynthetic
    fun insertPairingProposal(topic: String, uri: String, expirySeconds: Long, sequenceStatus: SequenceStatus, controllerType: ControllerType) {
        sessionDatabase.pairingDaoQueries.insertPairing(topic, uri, expirySeconds, sequenceStatus, controllerType)
    }

    @JvmSynthetic
    fun updatePendingPairingToSettled(proposalTopic: String, settledTopic: String, expirySeconds: Long, sequenceStatus: SequenceStatus) {
        sessionDatabase.pairingDaoQueries.updatePendingPairingToSettled(settledTopic, expirySeconds, sequenceStatus, proposalTopic)
    }

    @JvmSynthetic
    fun deletePairing(topic: String) {
        sessionDatabase.pairingDaoQueries.deletePairing(topic)
    }

    @JvmSynthetic
    fun insertSessionProposal(proposal: SessionParamsVO.Proposal, appMetaData: AppMetaDataVO?, defaultExpirySeconds: Long, controllerType: ControllerType) {
        val metadataId = insertMetaData(appMetaData)

        sessionDatabase.sessionDaoQueries.insertSession(
            topic = proposal.topic.value,
            permissions_chains = proposal.permissions.blockchain.chains,
            permissions_methods = proposal.permissions.jsonRpc.methods,
            permissions_types = proposal.permissions.notifications.types,
            ttl_seconds = proposal.ttl.seconds,
            expiry = defaultExpirySeconds,
            status = SequenceStatus.PENDING,
            controller_type = controllerType,
            metadata_id = metadataId
        )
    }

    private fun insertMetaData(appMetaData: AppMetaDataVO?): Long {
        return appMetaData?.let {
            sessionDatabase.metaDataDaoQueries.insertOrIgnoreMetaData(
                appMetaData.name,
                appMetaData.description,
                appMetaData.url,
                appMetaData.icons
            )

            sessionDatabase.metaDataDaoQueries.lastInsertedRowId().executeAsOne()
        } ?: FAILED_INSERT_ID
    }

    @JvmSynthetic
    fun updateStatusToSessionApproval(
        topicKey: String,
        subscriptionId: Long,
        settledTopic: String,
        accounts: List<String>,
        expirySeconds: Long
    ) {
        sessionDatabase.sessionDaoQueries.updateSessionWithSessionApproval(
            subscriptionId,
            settledTopic,
            accounts,
            expirySeconds,
            SequenceStatus.SETTLED,
            topicKey
        )
    }

    @JvmSynthetic
    fun updateSessionWithAccounts(topic: String, accounts: List<String>) {
        sessionDatabase.sessionDaoQueries.updateSessionWithAccounts(accounts, topic)
    }

    @JvmSynthetic
    fun updateSessionWithPermissions(topic: String, blockChains: List<String>?, jsonRpcMethods: List<String>?) {
        val (listOfChains, listOfMethods) = sessionDatabase.sessionDaoQueries.getPermissionsByTopic(topic).executeAsOne()
        val chainsUnion = listOfChains.union((blockChains ?: emptyList())).toList()
        val methodsUnion = listOfMethods.union((jsonRpcMethods ?: emptyList())).toList()
        sessionDatabase.sessionDaoQueries.updateSessionWithPermissions(chainsUnion, methodsUnion, topic)
    }

    @JvmSynthetic
    fun deleteSession(topic: String) {
        sessionDatabase.metaDataDaoQueries.deleteMetaDataFromTopic(topic)
        sessionDatabase.sessionDaoQueries.deleteSession(topic)
    }

    private fun mapPairingDaoToPairingVO(
        topic: String,
        expirySeconds: Long,
        uri: String,
        status: SequenceStatus,
        controller_type: ControllerType
    ): PairingVO {
        return PairingVO(TopicVO(topic), ExpiryVO(expirySeconds), uri, status)
    }

    private fun mapSessionDaoToSessionVO(
        topic: String,
        permission_chains: List<String>,
        permissions_methods: List<String>,
        permissions_types: List<String>,
        ttl_seconds: Long,
        accounts: List<String>?,
        expiry: Long,
        status: SequenceStatus,
        controller_type: ControllerType, // TODO: Figure out how to handle proposer and responder once proposer is implemented
        metadataName: String?,
        metadataDesc: String?,
        metadataUrl: String?,
        metadataIcons: List<String>?
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
            appMetaData = appMetaData
        )
    }

    private companion object {
        const val FAILED_INSERT_ID = -1L
    }
}