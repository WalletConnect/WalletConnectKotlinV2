@file:JvmSynthetic

package com.walletconnect.android.internal.common.storage.push_messages

import android.database.sqlite.SQLiteException
import com.walletconnect.android.sdk.storage.data.dao.PushMessageQueries

class PushMessageStorageRepository(private val pushMessageQueries: PushMessageQueries) {

    @Throws(SQLiteException::class)
    suspend fun insertPushMessage(id: Long, topic: String, blob: String) {
        pushMessageQueries.insertMessage(id, topic, blob)

    }

    @Throws(SQLiteException::class)
    suspend fun doesPushMessageExist(id: Long): Boolean {
        return pushMessageQueries.doesMessagesExistsByRequestId(id).executeAsOne()
    }
}