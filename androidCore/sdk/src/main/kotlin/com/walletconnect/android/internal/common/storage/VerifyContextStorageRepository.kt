package com.walletconnect.android.internal.common.storage

import android.database.sqlite.SQLiteException
import com.walletconnect.android.internal.common.model.Validation
import com.walletconnect.android.sdk.storage.data.dao.VerifyContextQueries
import com.walletconnect.android.verify.data.model.VerifyContext

class VerifyContextStorageRepository(private val verifyContextQueries: VerifyContextQueries) {

    @Throws(SQLiteException::class)
    fun insertOrAbort(verifyContext: VerifyContext) = with(verifyContext) {
        verifyContextQueries.insertOrAbortVerifyContext(id, origin, validation, verifyUrl)
    }

    @Throws(SQLiteException::class)
    fun get(id: Long): VerifyContext? {
        return verifyContextQueries.getVerifyContextById(id, mapper = this::toVerifyContext).executeAsOneOrNull()
    }

    @Throws(SQLiteException::class)
    fun getAll(): List<VerifyContext> {
        return verifyContextQueries.geListOfVerifyContexts(mapper = this::toVerifyContext).executeAsList()
    }

    @Throws(SQLiteException::class)
    fun delete(id: Long) {
        verifyContextQueries.deleteVerifyContext(id)
    }

    private fun toVerifyContext(id: Long, origin: String, validation: Validation, verifyUrl: String): VerifyContext =
        VerifyContext(id, origin, validation, verifyUrl)
}