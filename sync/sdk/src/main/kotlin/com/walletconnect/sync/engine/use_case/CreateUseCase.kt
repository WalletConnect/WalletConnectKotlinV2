package com.walletconnect.sync.engine.use_case

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.sync.common.model.Store

// This one is a class instead of object as it will need storage repository for store storage and entropy storage
internal class CreateUseCase() : CreateUseCaseInterface {

    override fun create(accountId: AccountId, store: Store) {
        TODO()
    }
}

internal interface CreateUseCaseInterface {
    fun create(accountId: AccountId, store: Store)
}
