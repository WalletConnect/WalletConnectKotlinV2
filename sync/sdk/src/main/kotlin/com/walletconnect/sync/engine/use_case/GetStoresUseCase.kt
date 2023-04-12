package com.walletconnect.sync.engine.use_case

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.sync.common.model.StoreMap

// This one is a class instead of object as it will need storage repository for store storage
internal class GetStoresUseCase() : GetStoresUseCaseInterface {

    override fun getStores(accountId: AccountId): StoreMap? {
        TODO()
    }
}

internal interface GetStoresUseCaseInterface {
    fun getStores(accountId: AccountId): StoreMap?
}
