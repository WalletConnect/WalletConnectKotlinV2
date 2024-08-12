package com.walletconnect.android.verify.domain

import android.database.sqlite.SQLiteException
import com.walletconnect.android.sdk.storage.data.dao.VerifyPublicKeyQueries

class VerifyPublicKeyStorageRepository(private val queries: VerifyPublicKeyQueries) {

    @Throws(SQLiteException::class)
    fun upsertPublicKey(publicKey: String, expiresAt: Long) {
        queries.upsertKey(publicKey, expiresAt)
    }

    @Throws(SQLiteException::class)
    fun getPublicKey(): Pair<String?, Long?> {
        return queries.getKey().executeAsOneOrNull().run {
            if (this == null) {
                Pair(null, null)
            } else {
                Pair(key, expires_at)
            }
        }
    }
}