package com.walletconnect.chat.storage

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.chat.common.model.Thread
import com.walletconnect.chat.storage.data.dao.ThreadsQueries
import com.walletconnect.foundation.common.model.Topic

internal class ThreadsStorageRepository(private val threads: ThreadsQueries) {
    suspend fun insertThread(topic: String, selfAccount: String, peerAccount: String) = threads.insertOrAbortThread(topic, selfAccount, peerAccount)

    suspend fun getThreadsForSelfAccount(account: String): List<Thread> = threads.getThreadsForSelfAccount(account, ::dbToThread).executeAsList()

    suspend fun checkIfSelfAccountHaveThreadWithPeerAccount(selfAccount: String, peerAccount: String): Boolean =
        threads.checkIfSelfAccountHaveThreadWithPeerAccount(selfAccount, peerAccount).executeAsOne()

    suspend fun deleteThreadByTopic(topic: String) = threads.deleteThreadByTopic(topic)

    suspend fun getThreadByTopic(topic: String): Thread = threads.getThreadByTopic(topic, ::dbToThread).executeAsOne()

    suspend fun getAllThreads(): List<Thread> = threads.getAllThreads(::dbToThread).executeAsList()

    private fun dbToThread(topic: String, selfPublicKey: String, peerPublicKey: String) = Thread(Topic(topic), AccountId(selfPublicKey), AccountId(peerPublicKey))
}