package com.walletconnect.sync.engine.use_case

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.sync.common.model.Store

// This one is a class instead of object as it will need storage repository for store storage
internal class SetUseCase() : SetUseCaseInterface {

    override fun set(accountId: AccountId, store: Store, key: String, value: String) {
        TODO()
    }
}

internal interface SetUseCaseInterface {
    fun set(accountId: AccountId, store: Store, key: String, value: String)
}
