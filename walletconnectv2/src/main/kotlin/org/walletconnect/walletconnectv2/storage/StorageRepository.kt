package org.walletconnect.walletconnectv2.storage

import android.app.Application
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.runtime.coroutines.*
import org.walletconnect.walletconnectv2.Database
import org.walletconnect.walletconnectv2.clientsync.session.Session
import org.walletconnect.walletconnectv2.clientsync.session.before.proposal.RelayProtocolOptions
import org.walletconnect.walletconnectv2.clientsync.session.before.proposal.SessionProposedPermissions
import org.walletconnect.walletconnectv2.clientsync.session.before.proposal.SessionProposer
import org.walletconnect.walletconnectv2.clientsync.session.before.proposal.SessionSignal
import org.walletconnect.walletconnectv2.clientsync.session.before.success.SessionParticipant
import org.walletconnect.walletconnectv2.clientsync.session.common.SessionState
import org.walletconnect.walletconnectv2.common.AppMetaData
import org.walletconnect.walletconnectv2.common.Expiry
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.common.Ttl
import org.walletconnect.walletconnectv2.scope
import org.walletconnect.walletconnectv2.storage.data.MetaDataDao
import org.walletconnect.walletconnectv2.storage.data.SessionDao

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
        MetaDataDaoAdapter = MetaDataDao.Adapter(iconsAdapter = listOfStringsAdapter),
        SessionDaoAdapter = SessionDao.Adapter(
            permissions_chainsAdapter = listOfStringsAdapter,
            permissions_methodsAdapter = listOfStringsAdapter,
            permissions_typesAdapter = listOfStringsAdapter,
            accountsAdapter = listOfStringsAdapter,
            statusAdapter = EnumColumnAdapter()
        )
    )
    //endregion

    val sessions = sessionDatabase.sessionQueries.getSessionDaoWithMetadata(mapper = this@StorageRepository::mapSessionsDaoWithMetadataToSession)
        .asFlow()
        .mapToList(scope.coroutineContext)


    fun updateStatusToSessionApproval(keyToSearch: String, selfPublicKey: String, updateTopic: String, listOfAccounts: List<String>, expiry: Long) {
        sessionDatabase.sessionQueries.updateSession(selfPublicKey, updateTopic, listOfAccounts, expiry, SequenceStatus.SETTLED, keyToSearch)
    }

    fun insertSessionProposal(proposal: Session.Proposal) {
        val metaDataRowId = insertOrGetMetaData(proposal.proposer.metadata)

        sessionDatabase.sessionQueries.insertSession(
            topic = proposal.topic.value,
            relay_protocol_option = proposal.relay.protocol,
            proposer_public_key = proposal.proposer.publicKey,
            proposer_is_controller = proposal.proposer.controller,
            metadata_id = metaDataRowId,
            signal_method = proposal.signal.method,
            signal_topic = proposal.signal.params.topic.value,
            permissions_chains = proposal.permissions.blockchain.chains,
            permissions_methods = proposal.permissions.jsonRpc.methods,
            permissions_types = proposal.permissions.notifications.types,
            ttl_seconds = proposal.ttl.seconds,
            status = SequenceStatus.PENDING
        )
    }

    fun insertOrGetMetaData(appMetaData: AppMetaData?): Long = with(sessionDatabase.metaDataQueries) {
        val insertedItemRowId = appMetaData?.let { metaData ->
            insertOrIgnoreMetaData(metaData.name, metaData.description, metaData.url, metaData.icons)
            lastInsertedRowId().executeAsOne()
        } ?: FAILED_INSERT_ID

        return insertedItemRowId
    }

    fun delete(selfPublicKeyString: String) {
        sessionDatabase.sessionQueries.deleteSession(selfPublicKeyString)
    }

    private fun mapSessionsDaoWithMetadataToSession(
        topic: String,
        relay_protocol_option: String,
        proposer_public_key: String,
        proposer_is_controller: Boolean,
        metadata_name: String?,
        metadata_desc: String?,
        metadata_url: String?,
        metadata_icons: List<String>?,
        signal_method: String,
        signal_topic: String,
        permission_chains: List<String>,
        permissions_methods: List<String>,
        permissions_types: List<String>,
        ttl_seconds: Long,
        self_public_key: String?,
        accounts: List<String>?,
        expiry: Long?,
        status: SequenceStatus
    ): Session {
        val metadata = if (metadata_name != null && metadata_desc != null && metadata_url != null && metadata_icons != null) {
            AppMetaData(metadata_name, metadata_desc, metadata_url, metadata_icons)
        } else {
            null
        }

        return if (status == SequenceStatus.PENDING) {
            Session.Proposal(
                topic = Topic(topic),
                relay = RelayProtocolOptions(protocol = relay_protocol_option),
                proposer = SessionProposer(publicKey = proposer_public_key, controller = proposer_is_controller, metadata = metadata),
                signal = SessionSignal(method = signal_method, params = SessionSignal.Params(topic = Topic(signal_topic))),
                permissions = SessionProposedPermissions(
                    blockchain = SessionProposedPermissions.Blockchain(chains = permission_chains),
                    jsonRpc = SessionProposedPermissions.JsonRpc(methods = permissions_methods),
                    notifications = SessionProposedPermissions.Notifications(types = permissions_types)
                ),
                ttl = Ttl(seconds = ttl_seconds)
            )
        } else {
            requireNotNull(self_public_key)
            requireNotNull(expiry)
            requireNotNull(accounts)

            Session.Success(
                relay = RelayProtocolOptions(protocol = relay_protocol_option),
                responder = SessionParticipant(publicKey = self_public_key, metadata = metadata),
                expiry = Expiry(expiry),
                state = SessionState(accounts = accounts)
            )
        }
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