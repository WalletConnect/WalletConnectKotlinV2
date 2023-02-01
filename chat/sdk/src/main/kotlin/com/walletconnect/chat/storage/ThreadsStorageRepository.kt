package com.walletconnect.chat.storage

import com.walletconnect.chat.common.model.Thread
import com.walletconnect.chat.storage.data.dao.ThreadsQueries

internal class ThreadsStorageRepository(private val threads: ThreadsQueries) {

    // TODO: Do we need to insert with the accountID?
    fun insertThread(topic: String, selfAccount: String, peerAccount: String) {
        threads.insertOrAbortThread(topic, selfAccount, peerAccount)
    }

    fun getThreadsForAccount(account: String): List<Thread> {
        return threads.getThreadsForAccount(account, account, ::dbToThread).executeAsList()
    }

    fun deleteThreadByTopic(topic: String) {
        threads.deleteThreadsByTopic(topic)
    }

    private fun dbToThread(
        topic: String,
        selfPublicKey: String,
        peerPublicKey: String
    ): Thread {
        return Thread(topic, selfPublicKey, peerPublicKey)
    }
}