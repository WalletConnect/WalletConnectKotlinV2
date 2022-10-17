package com.walletconnect.android.pairing

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.model.*
import com.walletconnect.foundation.common.model.Topic
import kotlinx.coroutines.flow.SharedFlow

//todo: add ( onError: (Core.Model.Error) -> Unit) parameter to accessible functions
interface PairingInterface {
    val selfMetaData: AppMetaData
    val topicExpiredFlow : SharedFlow<Topic>

    // initializes the client with persisted storage and a network connection
//    fun initialize(metaData: Core.Model.AppMetaData)
    // had to be removed to not be visible in CoreClient

    // for either to ping a peer
    fun ping(ping: Core.Params.Ping, sessionPing: Core.Listeners.SessionPing? = null)

    // for proposer to create inactive pairing
    fun create(): Result<Pairing> // todo: Maybe create Pairing data class?

    // for responder to pair a pairing created by a proposer
    fun pair(pair: Core.Params.Pair, onError: (Core.Model.Error) -> Unit = {})

    // query pairings
    fun getPairings(): List<Pairing>

    // for either peer to disconnect a pairing
    fun disconnect(topic: String, onError: (Core.Model.Error) -> Unit = {})

//    idea: --- I think those below shouldn't be accessible by SDK consumers.

    // for either to activate a previously created pairing
    fun activate(topic: String, onError: (Core.Model.Error) -> Unit = {})

    // for either to update the expiry of an existing pairing.
    fun updateExpiry(topic: String, expiry: Expiry, onError: (Core.Model.Error) -> Unit = {})

    // for either to update the metadata of an existing pairing.
    fun updateMetadata(topic: String, metadata: AppMetaData, metaDataType: AppMetaDataType, onError: (Core.Model.Error) -> Unit = {})

    // for both to subscribe on methods requests
    fun register(methods: String)

    interface Delegate {
        fun onPairingDelete(deletedPairing: Core.Model.DeletedPairing)
    }
}