package org.walletconnect.walletconnectv2.storage

import android.app.Application
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import org.walletconnect.walletconnectv2.Database
import org.walletconnect.walletconnectv2.clientsync.session.Session
import org.walletconnect.walletconnectv2.common.AppMetaData
import org.walletconnect.walletconnectv2.scope
import org.walletconnect.walletconnectv2.storage.data.MetaDataDao
import org.walletconnect.walletconnectv2.storage.data.SessionDao
import kotlin.coroutines.CoroutineContext

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
        MetaDataDaoAdapter = MetaDataDao.Adapter(iconAdapter = listOfStringsAdapter),
        SessionDaoAdapter = SessionDao.Adapter(
            permissions_chainAdapter = listOfStringsAdapter,
            permissions_methodsAdapter = listOfStringsAdapter,
            permissions_typesAdapter = listOfStringsAdapter,
            statusAdapter = EnumColumnAdapter()
        )
    )
    //endregion

    val sessions = sessionDatabase.sessionQueries.getSessionDao()
        .asFlow()
        .mapToOne(scope.coroutineContext)


    fun updateStatusToApprovalSession(topic: String, listOfAccounts: List<String>) {
//        db.updateSessionByTopic(topic, listOfAccounts, Enum.Approved)
    }

    @Deprecated("test function")
    fun getSesh(coroutineContext: CoroutineContext) = sessionDatabase.sessionQueries.getSessionDao()
        .asFlow()
        .mapToList(coroutineContext)

    fun insertSessionProposal(proposal: Session.Proposal) {
        val metaDataRowId = insertMetaData(proposal.proposer.metadata)

        sessionDatabase.sessionQueries.insertSession(
            topic = proposal.topic.topicValue,
            relay_protocol_option = proposal.relay.protocol,
            proposer_public_key = proposal.proposer.publicKey,
            proposer_is_controller = proposal.proposer.controller,
            metadata_id = metaDataRowId,
            signal_method = proposal.signal.method,
            signal_topic = proposal.signal.params.topic.topicValue,
            permissions_chain = proposal.permissions.blockchain.chains,
            permissions_methods = proposal.permissions.jsonRpc.methods,
            permissions_types = proposal.permissions.notifications.types,
            ttl_seconds = proposal.ttl.seconds,
            status = SequenceStatus.PENDING
        )
    }

//    fun insertSessionBlockchains(blockchain: SessionProposedPermissions.Blockchain): Long = with(sessionDatabase.sessionBlockchainsQueries) {
//        insertSessionBlockchains(blockchain.chains)
//
//        return lastInsertedRowId().executeAsOneOrNull() ?: FAILED_INSERT_ID
//    }
//
//    fun insertSessionJsonRpcMethods(jsonRpcMethods: SessionProposedPermissions.JsonRpc): Long = with(sessionDatabase.sessionJsonRpcMethodsQueries) {
//        insertSessionJsonRpcMethods(jsonRpcMethods.methods)
//
//        return lastInsertedRowId().executeAsOneOrNull() ?: FAILED_INSERT_ID
//    }
//
//    fun insertSessionNotifications(notifications: SessionProposedPermissions.Notifications): Long = with(sessionDatabase.sessionNotificationsQueries) {
//        insertSessionNotifications(notifications.types)
//
//        return lastInsertedRowId().executeAsOneOrNull() ?: FAILED_INSERT_ID
//    }
//
//    fun insertSessionSignal(sessionSignal: SessionSignal): Long = with(sessionDatabase.sessionSignalQueries) {
//        insertSessionSignal(sessionSignal.method, sessionSignal.params.topic.topicValue)
//
//        return lastInsertedRowId().executeAsOneOrNull() ?: FAILED_INSERT_ID
//    }
//
//    fun insertSessionProposer(proposer: SessionProposer): Long = with(sessionDatabase.sessionProposerQueries) {
//        val metaDataRowId = insertMetaData(proposer.metadata)
//        insertSessionProposer(proposer.publicKey, proposer.controller, metaDataRowId)
//
//        return lastInsertedRowId().executeAsOneOrNull() ?: FAILED_INSERT_ID
//    }

    fun insertMetaData(appMetaData: AppMetaData?): Long = with(sessionDatabase.metaDataQueries) {
        val insertedItemRowId = appMetaData?.let { metaData ->
            insertOrIgnoreMetaData(metaData.name, metaData.description, metaData.url, metaData.icons)
            lastInsertedRowId().executeAsOneOrNull() ?: FAILED_INSERT_ID
        } ?: FAILED_INSERT_ID

        return insertedItemRowId
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