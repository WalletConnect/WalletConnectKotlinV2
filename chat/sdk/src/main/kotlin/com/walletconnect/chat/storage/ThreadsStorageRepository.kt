package com.walletconnect.chat.storage

import com.walletconnect.chat.common.model.AccountId
import com.walletconnect.chat.common.model.Thread
import com.walletconnect.chat.storage.data.dao.ThreadsQueries
import com.walletconnect.foundation.common.model.Topic

internal class ThreadsStorageRepository(private val threads: ThreadsQueries) {
    fun insertThread(topic: String, selfAccount: String, peerAccount: String) = threads.insertOrAbortThread(topic, selfAccount, peerAccount)
    fun getThreadsForSelfAccount(account: String): List<Thread> = threads.getThreadsForSelfAccount(account, ::dbToThread).executeAsList()
    fun deleteThreadByTopic(topic: String) = threads.deleteThreadByTopic(topic)
    fun getThreadByTopic(topic: String): Thread = threads.getThreadByTopic(topic, ::dbToThread).executeAsOne()
    fun getAllThreads(): List<Thread> = threads.getAllThreads(::dbToThread).executeAsList()
    private fun dbToThread(topic: String, selfPublicKey: String, peerPublicKey: String) = Thread(Topic(topic), AccountId(selfPublicKey), AccountId(peerPublicKey))
}