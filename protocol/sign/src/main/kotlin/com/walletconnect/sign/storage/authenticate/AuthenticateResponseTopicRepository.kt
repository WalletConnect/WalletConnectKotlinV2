package com.walletconnect.sign.storage.authenticate

import android.database.sqlite.SQLiteException
import com.walletconnect.sign.storage.data.dao.authenticatereponse.AuthenticateResponseTopicDaoQueries

internal class AuthenticateResponseTopicRepository(private val authenticateResponseTopicDaoQueries: AuthenticateResponseTopicDaoQueries) {
    @JvmSynthetic
    @Throws(SQLiteException::class)
    suspend fun insertOrAbort(pairingTopic: String, responseTopic: String) {
        authenticateResponseTopicDaoQueries.insertOrAbort(pairingTopic, responseTopic)
    }

    @JvmSynthetic
    @Throws(SQLiteException::class)
    suspend fun delete(pairingTopic: String) {
        authenticateResponseTopicDaoQueries.deleteByPairingTopic(pairingTopic)
    }

    @JvmSynthetic
    @Throws(SQLiteException::class)
    suspend fun getResponseTopics(): List<String> {
        return authenticateResponseTopicDaoQueries.getListOfTopics().executeAsList().map { it.responseTopic }
    }
}