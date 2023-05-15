package com.walletconnect.android.sync.storage

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.sdk.storage.data.dao.sync.StoreValuesQueries
import com.walletconnect.android.sdk.storage.data.dao.sync.StoresQueries
import com.walletconnect.android.sync.common.model.Store
import com.walletconnect.android.sync.common.model.StoreMap
import com.walletconnect.android.sync.common.model.StoreState
import com.walletconnect.foundation.common.model.Topic

internal class StoresStorageRepository(private val stores: StoresQueries, private val storeValues: StoreValuesQueries) {

    suspend fun createStore(accountId: AccountId, store: Store, symmetricKey: SymmetricKey, topic: Topic) {
        // Only insert when store does not yet exists in db. SymKey&Topic will be the same so no need for multiple inserts/updates
        stores.insertOrAbortStore(accountId = accountId.value, name = store.value, symKey = symmetricKey.keyAsHex, topic = topic.value)
    }

    suspend fun getAllTopics(): List<Topic> = stores.getAllTopics().executeAsList().map { Topic(it) }

    suspend fun getStoreMap(accountId: AccountId): StoreMap = stores.getStoresByAccountId(accountId.value).executeAsList().map { it.name to getStoreValuesByStoreId(it.id) } as StoreMap

    suspend fun getStoreValue(accountId: AccountId, store: Store, key: String): Pair<String, String> =
        storeValues.getStoreValueByStoreIdAndKey(getStoreId(accountId, store), key, ::dbToStoreValue).executeAsOne()

    suspend fun deleteStoreValue(accountId: AccountId, store: Store, key: String) = storeValues.deleteStoreValue(getStoreId(accountId, store), key)

    suspend fun upsertStoreValue(accountId: AccountId, store: Store, key: String, value: String) = getStoreId(accountId, store).let { storeId ->
        if (doesStoreValueNotExists(storeId, key)) {
            insertStoreValue(storeId, key, value)
        } else {
            updateStoreValue(storeId, key, value)
        }
    }

    suspend fun getStoreTopic(accountId: AccountId, store: Store): String = stores.getStoreTopicByAccountIdAndName(accountId.value, store.value).executeAsOne()

    suspend fun getAccountIdAndStoreByTopic(topic: Topic): Pair<AccountId, Store> = stores.getStoreByTopic(topic.value).executeAsOne().let { AccountId(it.accountId) to Store(it.name) }

    private suspend fun insertStoreValue(storeId: Long, key: String, value: String) = storeValues.insertOrAbortStoreValue(storeId, key, value)

    private suspend fun updateStoreValue(storeId: Long, key: String, value: String) = storeValues.updateStoreValue(value, storeId, key)

    private suspend fun getStoreId(accountId: AccountId, store: Store): Long = stores.getStoreIdByAccountIdAndName(accountId.value, store.value).executeAsOne()

    private suspend fun getStoreValuesByStoreId(storeId: Long): StoreState = storeValues.getStoreValuesByStoreId(storeId).executeAsList().map { it.key to it.value_ } as StoreState

    suspend fun doesStoreNotExists(accountId: AccountId, store: Store) = stores.doesStoreNotExists(accountId.value, store.value).executeAsOne()

    private suspend fun doesStoreValueNotExists(storeId: Long, key: String) = storeValues.doesStoreValueNotExists(storeId, key).executeAsOne()

    private fun dbToStoreValue(key: String, value: String): Pair<String, String> = key to value
}
