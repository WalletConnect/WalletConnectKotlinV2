package com.walletconnect.chat.storage

import com.walletconnect.chat.common.model.Thread
import com.walletconnect.chat.storage.data.dao.ThreadsQueries

internal class ThreadsStorageRepository(private val threads: ThreadsQueries) {

    fun insertThread(topic: String, selfAccount: String, peerAccount: String) {
        threads.insertOrAbortThread(topic, selfAccount, peerAccount)
    }

    fun getThreadsForSelfAccount(account: String): List<Thread> {
        return threads.getThreadsForSelfAccount(account, ::dbToThread).executeAsList()
    }

    fun deleteThreadByTopic(topic: String) {
        threads.deleteThreadByTopic(topic)
    }

    fun getThreadByTopic(topic: String): Thread {
        return threads.getThreadByTopic(topic, ::dbToThread).executeAsOne()
    }

    private fun dbToThread(
        topic: String,
        selfPublicKey: String,
        peerPublicKey: String
    ): Thread {
        return Thread(topic, selfPublicKey, peerPublicKey)
    }
}