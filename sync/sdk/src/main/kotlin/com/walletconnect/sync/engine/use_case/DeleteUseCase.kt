package com.walletconnect.sync.engine.use_case

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.sync.common.model.Store

// This one is a class instead of object as it will need storage repository for store storage
internal class DeleteUseCase() : DeleteUseCaseInterface {

    override fun delete(accountId: AccountId, store: Store, key: String) {
        TODO()
    }
}

internal interface DeleteUseCaseInterface {
    fun delete(accountId: AccountId, store: Store, key: String)
}
