package com.walletconnect.walletconnectv2.storage.sequence

import android.app.Application
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.walletconnect.walletconnectv2.Database
import com.walletconnect.walletconnectv2.common.model.type.ControllerType
import com.walletconnect.walletconnectv2.common.model.vo.*
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.proposal.AppMetaDataVO
import com.walletconnect.walletconnectv2.common.model.vo.sequence.*
import com.walletconnect.walletconnectv2.common.model.vo.sequence.PairingVO
import com.walletconnect.walletconnectv2.util.Empty
import org.walletconnect.walletconnectv2.storage.data.dao.MetaDataDao
import org.walletconnect.walletconnectv2.storage.data.dao.PairingDao
import org.walletconnect.walletconnectv2.storage.data.dao.SessionDao

//TODO: Split into SessionStorageRepository and PairingStorageRepository
internal class SequenceStorageRepository constructor(sqliteDriver: SqlDriver?, application: Application) {
    //region provide with DI
    // TODO: once DI is setup, replace var with val
    private val driver = sqliteDriver ?: AndroidSqliteDriver(
        schema = Database.Schema,
        context = application,
        name = "WalletConnectV2.db"
    )
    private val sequenceDatabase: Database = Database(
        driver,
        PairingDaoAdapter = PairingDao.Adapter(
            statusAdapter = EnumColumnAdapter(),
            controller_typeAdapter = EnumColumnAdapter(),
            permissionsAdapter = listOfStringsAdapter
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

    fun getListOfPairingVOs() =
        sequenceDatabase.pairingDaoQueries.getListOfPairingDaos(mapper = this@SequenceStorageRepository::mapPairingDaoToPairingVO)
            .executeAsList()

    fun getListOfSessionVOs() =
        sequenceDatabase.sessionDaoQueries.getListOfSessionDaos(mapper = this@SequenceStorageRepository::mapSessionDaoToSessionVO)
            .executeAsList()

    fun getPairingByTopic(topic: TopicVO): PairingVO? =
        sequenceDatabase.pairingDaoQueries.getPairingByTopic(topic.value)
            .executeAsOneOrNull()?.let { entity ->
                PairingVO(
                    topic = TopicVO(entity.topic),
                    status = entity.status,
                    expiry = ExpiryVO(entity.expiry),
                    selfParticipant = PublicKey(entity.self_participant),
                    peerParticipant = PublicKey(entity.peer_participant ?: String.Empty),
                    controllerKey = PublicKey(entity.controller_key ?: String.Empty),
                    proposalUri = entity.uri,
                    permissions = entity.permissions,
                    relayProtocol = entity.relay_protocol
                )
            }

    fun getSessionByTopic(topic: TopicVO): SessionVO? =
        sequenceDatabase.sessionDaoQueries.getSessionByTopic(topic.value)
            .executeAsOneOrNull()?.let { entity ->
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
                    controllerType = entity.controller_type,
                    relayProtocol = entity.relay_protocol
                )
            }

    fun insertPendingPairing(pairing: PairingVO, controllerType: ControllerType) {
        with(pairing) {
            sequenceDatabase.pairingDaoQueries.insertPairing(
                topic.value,
                proposalUri,
                expiry.seconds,
                status,
                controllerType,
                selfParticipant.keyAsHex,
                relayProtocol
            )
        }
    }

    fun updateRespondedPairingToPreSettled(proposalTopic: TopicVO, pairing: PairingVO) {
        with(pairing) {
            sequenceDatabase.pairingDaoQueries.updatePendingPairingToPreSettled(
                topic.value,
                expiry.seconds,
                status,
                selfParticipant.keyAsHex,
                peerParticipant?.keyAsHex,
                controllerKey?.keyAsHex,
                permissions,
                proposalTopic.value
            )
        }
    }

    fun updatePreSettledPairingToAcknowledged(pairing: PairingVO) {
        sequenceDatabase.pairingDaoQueries.updatePreSettledPairingToAcknowledged(pairing.status, pairing.topic.value)
    }

    fun updateProposedPairingToAcknowledged(pairing: PairingVO, pendingTopic: TopicVO) {
        with(pairing) {
            sequenceDatabase.pairingDaoQueries.updateProposedPairingToAcknowledged(
                pairing.topic.value,
                expiry.seconds,
                status,
                selfParticipant.keyAsHex,
                peerParticipant?.keyAsHex,
                controllerKey?.keyAsHex,
                permissions,
                pendingTopic.value,
                relayProtocol
            )
        }
    }

    fun deletePairing(topic: TopicVO) {
        sequenceDatabase.pairingDaoQueries.deletePairing(topic.value)
    }

    fun insertSessionProposal(session: SessionVO, appMetaData: AppMetaDataVO?, controllerType: ControllerType) {
        val metadataId = insertMetaData(appMetaData)

        with(session) {
            sequenceDatabase.sessionDaoQueries.insertSession(
                topic = topic.value,
                permissions_chains = chains,
                permissions_methods = methods,
                permissions_types = types,
                ttl_seconds = ttl.seconds,
                expiry = expiry.seconds,
                status = status,
                controller_type = controllerType,
                metadata_id = metadataId,
                self_participant = selfParticipant.keyAsHex,
                relay_protocol = session.relayProtocol
            )
        }
    }

    private fun insertMetaData(appMetaData: AppMetaDataVO?): Long {
        return appMetaData?.let {
            sequenceDatabase.metaDataDaoQueries.insertOrIgnoreMetaData(
                appMetaData.name,
                appMetaData.description,
                appMetaData.url,
                appMetaData.icons
            )

            sequenceDatabase.metaDataDaoQueries.lastInsertedRowId().executeAsOne()
        } ?: FAILED_INSERT_ID
    }

    fun updateProposedSessionToResponded(session: SessionVO) {
        sequenceDatabase.sessionDaoQueries.updateProposedSessionToResponded(session.status, session.topic.value)
    }

    fun updateRespondedSessionToPreSettled(session: SessionVO, pendingTopic: TopicVO) {
        with(session) {
            sequenceDatabase.sessionDaoQueries.updateRespondedSessionToPresettled(
                topic.value,
                accounts,
                expiry.seconds,
                status,
                selfParticipant.keyAsHex,
                controllerKey?.keyAsHex,
                peerParticipant?.keyAsHex,
                chains,
                methods,
                types,
                ttl.seconds,
                pendingTopic.value
            )
        }
    }

    fun updatePreSettledSessionToAcknowledged(session: SessionVO) {
        sequenceDatabase.sessionDaoQueries.updatePreSettledSessionToAcknowledged(session.status, session.topic.value)
    }

    fun updateProposedSessionToAcknowledged(session: SessionVO, pendingTopic: TopicVO) {

        val metadataId = insertMetaData(session.appMetaData)

        with(session) {
            sequenceDatabase.sessionDaoQueries.updateProposedSessionToAcknowledged(
                topic.value,
                accounts,
                expiry.seconds,
                status,
                selfParticipant.keyAsHex,
                controllerKey?.keyAsHex,
                peerParticipant?.keyAsHex,
                chains,
                methods,
                types,
                ttl.seconds,
                relayProtocol,
                metadataId,
                pendingTopic.value
            )
        }
    }

    fun updateSessionWithAccounts(topic: String, accounts: List<String>) {
        sequenceDatabase.sessionDaoQueries.updateSessionWithAccounts(accounts, topic)
    }

    fun updateSessionWithPermissions(topic: String, blockChains: List<String>?, jsonRpcMethods: List<String>?) {
        val (listOfChains, listOfMethods) = sequenceDatabase.sessionDaoQueries.getPermissionsByTopic(topic).executeAsOne()
        val chainsUnion = listOfChains.union((blockChains ?: emptyList())).toList()
        val methodsUnion = listOfMethods.union((jsonRpcMethods ?: emptyList())).toList()
        sequenceDatabase.sessionDaoQueries.updateSessionWithPermissions(chainsUnion, methodsUnion, topic)
    }

    fun deleteSession(topic: TopicVO) {
        sequenceDatabase.metaDataDaoQueries.deleteMetaDataFromTopic(topic.value)
        sequenceDatabase.sessionDaoQueries.deleteSession(topic.value)
    }

    private fun mapPairingDaoToPairingVO(
        topic: String,
        expirySeconds: Long,
        uri: String,
        status: SequenceStatus,
        controller_type: ControllerType,
        self_participant: String,
        peer_participant: String?,
        controller_key: String?,
        relay_protocol: String,
        permissions: List<String>?
    ): PairingVO =
        PairingVO(
            topic = TopicVO(topic),
            expiry = ExpiryVO(expirySeconds),
            status = status,
            selfParticipant = PublicKey(self_participant),
            peerParticipant = PublicKey(peer_participant ?: String.Empty),
            permissions = permissions,
            controllerKey = PublicKey(controller_key ?: String.Empty),
            proposalUri = uri,
            relayProtocol = relay_protocol
        )

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
            appMetaData = appMetaData,
            selfParticipant = PublicKey(self_participant),
            peerParticipant = PublicKey(peer_participant ?: String.Empty),
            controllerKey = PublicKey(controller_key ?: String.Empty),
            controllerType = controller_type,
            relayProtocol = relay_protocol
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