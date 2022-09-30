package com.walletconnect.android.pairing

import android.database.sqlite.SQLiteException
import com.walletconnect.android.Core
import com.walletconnect.android.common.crypto.KeyManagementRepository
import com.walletconnect.android.common.model.*
import com.walletconnect.android.common.wcKoinApp
import com.walletconnect.android.exception.CannotFindSequenceForTopic
import com.walletconnect.android.exception.MalformedWalletConnectUri
import com.walletconnect.android.exception.PairWithExistingPairingIsNotAllowed
import com.walletconnect.android.internal.MALFORMED_PAIRING_URI_MESSAGE
import com.walletconnect.android.internal.NO_SEQUENCE_FOR_TOPIC_MESSAGE
import com.walletconnect.android.internal.PAIRING_NOW_ALLOWED_MESSAGE
import com.walletconnect.android.internal.Validator
import com.walletconnect.android.relay.RelayConnectionInterface
import com.walletconnect.android.utils.isSequenceValid
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.randomBytes

internal object PairingClient : PairingInterface {
    private lateinit var _selfMetaData: Core.Model.AppMetaData
    override val selfMetaData: Core.Model.AppMetaData
        get() = _selfMetaData

    private val storageRepository: PairingStorageRepository
        get() = wcKoinApp.koin.getOrNull() ?: throw IllegalStateException("SDK has not been initialized")
    private val crypto: KeyManagementRepository
        get() = wcKoinApp.koin.getOrNull() ?: throw IllegalStateException("SDK has not been initialized")
    private val relayer: RelayConnectionInterface
        get() = wcKoinApp.koin.getOrNull() ?: throw IllegalStateException("SDK has not been initialized")

    override fun initialize(metaData: Core.Model.AppMetaData) {
        _selfMetaData = metaData
    }

    // TODO: add parameter for RelayClient to publish ping request, pass listener functions into RelayClient's onSuccess and onFailure methods
    override fun ping(ping: Core.Params.Ping, sessionPing: Core.Listeners.SessionPing?) {
        if (storageRepository.isPairingValid(Topic(ping.topic))) {
//        sessionPing?.onSuccess(Core.Model.Ping.Success())
        } else {
//        sessionPing?.onError(Core.Model.Ping.Error())
        }
        relayer.publish()
    }

    override fun create(onPairingCreated: (String) -> Unit, onError: (Core.Model.Error) -> Unit) {
        val pairingTopic: Topic = generateTopic()
        val symmetricKey: SymmetricKey = crypto.generateAndStoreSymmetricKey(pairingTopic)
        val relay = RelayProtocolOptions()
        val walletConnectUri = WalletConnectUri(pairingTopic, symmetricKey, relay)
        val inactivePairing = Pairing(walletConnectUri)

        try {
            storageRepository.insertPairing(inactivePairing)
            onPairingCreated(pairingTopic.value)
        } catch (e: Exception) {
            crypto.removeKeys(pairingTopic.value)
            storageRepository.deletePairing(pairingTopic)

            onError(Core.Model.Error(e))
        }
    }

    override fun pair(pair: Core.Params.Pair, onError: (Core.Model.Error) -> Unit) {
        val walletConnectUri: WalletConnectUri = Validator.validateWCUri(pair.uri) ?: return onError(Core.Model.Error(MalformedWalletConnectUri(MALFORMED_PAIRING_URI_MESSAGE)))

        if (storageRepository.isPairingValid(walletConnectUri.topic)) {
            return onError(Core.Model.Error(PairWithExistingPairingIsNotAllowed(PAIRING_NOW_ALLOWED_MESSAGE)))
        }

        val activePairing = Pairing(walletConnectUri)
        val symmetricKey = walletConnectUri.symKey
        crypto.setSymmetricKey(walletConnectUri.topic, symmetricKey)

        try {
            storageRepository.insertPairing(activePairing)
        } catch (e: SQLiteException) {
            crypto.removeKeys(walletConnectUri.topic.value)
            onError(Core.Model.Error(e))
        }
    }

    override fun getPairings(): List<Pairing> {
        return storageRepository.getListOfPairingVOs()
            .filter { pairing -> pairing.expiry.isSequenceValid() }
    }

    // TODO: add parameter to unsubscribe and publish SessionDelete from the RelayClient
    override fun disconnect(topic: String, onError: (Core.Model.Error) -> Unit) {
        if (!storageRepository.isPairingValid(Topic(topic))) {
            return onError(Core.Model.Error(CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")))
        }

        storageRepository.deletePairing(Topic(topic))
        // RelayClient code here
    }

    override fun activate(topic: String, onError: (Core.Model.Error) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun updateExpiry(topic: String, expiry: Expiry, onError: (Core.Model.Error) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun updateMetadata(topic: String, metadata: PeerMetaData, onError: (Core.Model.Error) -> Unit) {
        TODO("Not yet implemented")
    }

    private val methodsToCallbacks: MutableMap<String, (topic: String, request: WCRequest) -> Unit> = mutableMapOf()

    @Throws(MethodAlreadyRegistered::class)
    override fun register(method: String, onMethod: (topic: String, request: WCRequest) -> Unit) {
        if (methodsToCallbacks.containsKey(method)) throw MethodAlreadyRegistered("Method: $method already registered")

        methodsToCallbacks[method] = onMethod
    }

    private fun generateTopic(): Topic = Topic(randomBytes(32).bytesToHex())
}