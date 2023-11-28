package com.walletconnect.android.internal.common.storage.verify

import android.database.sqlite.SQLiteException
import com.walletconnect.android.internal.common.model.Validation
import com.walletconnect.android.sdk.storage.data.dao.VerifyContextQueries
import com.walletconnect.android.verify.data.model.VerifyContext

class VerifyContextStorageRepository(private val verifyContextQueries: VerifyContextQueries) {

    @Throws(SQLiteException::class)
    suspend fun insertOrAbort(verifyContext: VerifyContext) = with(verifyContext) {
        verifyContextQueries.insertOrAbortVerifyContext(id, origin, validation, verifyUrl, isScam)
    }

    @Throws(SQLiteException::class)
    suspend fun get(id: Long): VerifyContext? {
        return verifyContextQueries.getVerifyContextById(id, mapper = this::toVerifyContext).executeAsOneOrNull()
    }

    @Throws(SQLiteException::class)
    suspend fun getAll(): List<VerifyContext> {
        return verifyContextQueries.geListOfVerifyContexts(mapper = this::toVerifyContext).executeAsList()
    }

    @Throws(SQLiteException::class)
    suspend fun delete(id: Long) {
        verifyContextQueries.deleteVerifyContext(id)
    }

    private fun toVerifyContext(id: Long, origin: String, validation: Validation, verifyUrl: String, isScam: Boolean?): VerifyContext =
        VerifyContext(id, origin, validation, verifyUrl, isScam)
}