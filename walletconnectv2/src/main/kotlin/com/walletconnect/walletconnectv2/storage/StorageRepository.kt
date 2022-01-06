package com.walletconnect.walletconnectv2.storage

import com.squareup.sqldelight.runtime.coroutines.*
import com.walletconnect.walletconnectv2.Database
import com.walletconnect.walletconnectv2.clientsync.session.Session
import com.walletconnect.walletconnectv2.common.*
import com.walletconnect.walletconnectv2.storage.data.vo.AppMetaDataVO
import com.walletconnect.walletconnectv2.storage.data.vo.PairingVO
import com.walletconnect.walletconnectv2.storage.data.vo.SessionVO
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class StorageRepository @Inject constructor(private val sessionDatabase: Database) {

    fun getListOfPairingVOs() =
        sessionDatabase.pairingDaoQueries.getListOfPairingDaos(mapper = this@StorageRepository::mapPairingDaoToPairingVO).executeAsList()

    fun getListOfSessionVOs() =
        sessionDatabase.sessionDaoQueries.getListOfSessionDaos(mapper = this@StorageRepository::mapSessionDaoToSessionVO).executeAsList()

    fun insertPairingProposal(topic: String, uri: String, expirySeconds: Long, sequenceStatus: SequenceStatus, controllerType: ControllerType) {
        sessionDatabase.pairingDaoQueries.insertPairing(topic, uri, expirySeconds, sequenceStatus, controllerType)
    }

    fun updatePendingPairingToSettled(proposalTopic: String, settledTopic: String, expirySeconds: Long, sequenceStatus: SequenceStatus) {
        sessionDatabase.pairingDaoQueries.updatePendingPairingToSettled(settledTopic, expirySeconds, sequenceStatus, proposalTopic)
    }

    fun deletePairing(topic: String) {
        sessionDatabase.pairingDaoQueries.deletePairing(topic)
    }

    fun insertSessionProposal(proposal: Session.Proposal, appMetaData: AppMetaData?, defaultExpirySeconds: Long, controllerType: ControllerType) {
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

    private fun insertMetaData(appMetaData: AppMetaData?): Long {
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

    fun updateSessionWithAccounts(topic: String, accounts: List<String>) {
        sessionDatabase.sessionDaoQueries.updateSessionWithAccounts(accounts, topic)
    }

    fun updateSessionWithPermissions(topic: String, blockChains: List<String>?, jsonRpcMethods: List<String>?) {
        val (listOfChains, listOfMethods) = sessionDatabase.sessionDaoQueries.getPermissionsByTopic(topic).executeAsOne()
        val chainsUnion = listOfChains.union((blockChains ?: emptyList())).toList()
        val methodsUnion = listOfMethods.union((jsonRpcMethods ?: emptyList())).toList()
        sessionDatabase.sessionDaoQueries.updateSessionWithPermissions(chainsUnion, methodsUnion, topic)
    }

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
        return PairingVO(Topic(topic), Expiry(expirySeconds), uri, status)
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
            topic = Topic(topic),
            chains = permission_chains,
            methods = permissions_methods,
            types = permissions_types,
            ttl = Ttl(ttl_seconds),
            accounts = accounts ?: emptyList(),
            expiry = Expiry(expiry),
            status = status,
            appMetaData = appMetaData
        )
    }

    companion object {
        private const val FAILED_INSERT_ID = -1L
    }
}