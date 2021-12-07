package org.walletconnect.walletconnectv2.storage

import android.app.Application
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.runtime.coroutines.*
import org.walletconnect.walletconnectv2.Database
import org.walletconnect.walletconnectv2.clientsync.session.Session
import org.walletconnect.walletconnectv2.common.*
import org.walletconnect.walletconnectv2.scope
import org.walletconnect.walletconnectv2.storage.data.dao.MetaDataDao
import org.walletconnect.walletconnectv2.storage.data.dao.PairingDao
import org.walletconnect.walletconnectv2.storage.data.dao.SessionDao
import org.walletconnect.walletconnectv2.storage.data.vo.AppMetaDataVO
import org.walletconnect.walletconnectv2.storage.data.vo.PairingVO
import org.walletconnect.walletconnectv2.storage.data.vo.SessionVO
import kotlin.math.exp

internal class StorageRepository constructor(sqliteDriver: SqlDriver?, application: Application) {
    //region provide with DI
    // TODO: once DI is setup, replace var with val
    private val driver = sqliteDriver ?: AndroidSqliteDriver(
        schema = Database.Schema,
        context = application,
        name = "WalletConnectV2.db"
    )
    private val sessionDatabase: Database = Database(
        driver,
        PairingDaoAdapter = PairingDao.Adapter(
            statusAdapter = EnumColumnAdapter(),
            controller_typeAdapter = EnumColumnAdapter()
        ),
        SessionDaoAdapter = SessionDao.Adapter(
            permissions_chainsAdapter = listOfStringsAdapter,
            permissions_methodsAdapter = listOfStringsAdapter,
            permissions_typesAdapter = listOfStringsAdapter,
            accountsAdapter = listOfStringsAdapter,
            statusAdapter = EnumColumnAdapter(),
            controller_typeAdapter = EnumColumnAdapter()
        ),
        MetaDataDaoAdapter = MetaDataDao.Adapter(iconsAdapter = listOfStringsAdapter)
    )
    //endregion

    val listOfSessionVO = sessionDatabase.sessionDaoQueries.getSessionDao(mapper = this@StorageRepository::mapSessionDaoToSessionVO).executeAsList()

    fun insertPairingProposal(topic: String, uri: String, sequenceStatus: SequenceStatus, controllerType: ControllerType) {
        sessionDatabase.pairingDaoQueries.insertPairing(topic, uri, sequenceStatus, controllerType)
    }

    fun updatePendingPairingToSettled(proposalTopic: String, settledTopic: String, expirySeconds: Long, sequenceStatus: SequenceStatus) {
        sessionDatabase.pairingDaoQueries.updatePendingPairingToSettled(settledTopic, expirySeconds, sequenceStatus, proposalTopic)
    }

    fun insertSessionProposal(proposal: Session.Proposal, appMetaData: AppMetaData?, controllerType: ControllerType) {
        val metadataId = insertMetaData(appMetaData)

        sessionDatabase.sessionDaoQueries.insertSession(
            topic = proposal.topic.value,
            permissions_chains = proposal.permissions.blockchain.chains,
            permissions_methods = proposal.permissions.jsonRpc.methods,
            permissions_types = proposal.permissions.notifications.types,
            ttl_seconds = proposal.ttl.seconds,
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

    fun getMetaData(): AppMetaData {
        return sessionDatabase.metaDataDaoQueries.getMetaData(mapper = { _, name, description, url, listOfIcons ->
            AppMetaData(name, description, url, listOfIcons)
        }).executeAsOne()
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
        val chains = blockChains ?: emptyList()
        val methods = jsonRpcMethods ?: emptyList()

        sessionDatabase.sessionDaoQueries.updateSessionWithPermissions(chains, methods, topic)
    }

    fun delete(topic: String) {
        sessionDatabase.sessionDaoQueries.deleteSession(topic)
    }

    private fun mapPairingDaoToPairingVO(
        topic: String,
        expirySeconds: Long?,
        uri: String,
        status: SequenceStatus,
        controller_type: ControllerType
    ): PairingVO {
        val expiry = if (expirySeconds != null) Expiry(expirySeconds) else null
        return PairingVO(Topic(topic), expiry, uri, status)
    }

    private fun mapSessionDaoToSessionVO(
        topic: String,
        permission_chains: List<String>,
        permissions_methods: List<String>,
        permissions_types: List<String>,
        ttl_seconds: Long,
        accounts: List<String>?,
        expiry: Long?,
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
            expiry = if (expiry != null) Expiry(expiry) else null,
            status = status,
            appMetaData = appMetaData
        )
    }

    companion object {
        private const val FAILED_INSERT_ID = -1L
        internal val listOfStringsAdapter = object : ColumnAdapter<List<String>, String> {

            override fun decode(databaseValue: String) =
                if (databaseValue.isEmpty()) {
                    listOf()
                } else {
                    databaseValue.split(",")
                }

            override fun encode(value: List<String>) = value.joinToString(separator = ",")
        }
    }
}