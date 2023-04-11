package com.walletconnect.sync.client

import androidx.annotation.Keep
import com.walletconnect.android.CoreClient
import com.walletconnect.android.cacao.SignatureInterface
import com.walletconnect.android.internal.common.model.AccountId

object Sync {
    sealed class Events {
        data class OnSyncUpdate(val store: Type.Store, val update: Model.SyncUpdate) : Events()
    }

    sealed class Type {
        @JvmInline
        value class Store(val value: String)

        interface StoreState : Map<String, String>

        interface StoreMap : Map<String, StoreState>
    }

    sealed class Model {
        sealed interface SyncUpdate {
            val id: Long
            val key: String

            data class SyncSet(override val id: Long, override val key: String, val value: String) : SyncUpdate

            data class SyncDelete(override val id: Long, override val key: String) : SyncUpdate
        }

        data class Error(val throwable: Throwable) : Model()

        data class ConnectionState(val isAvailable: Boolean) : Model()

        @Keep
        data class Signature(override val t: String, override val s: String, override val m: String? = null) : Model(), SignatureInterface
    }

    sealed class Params {
        data class Init(val core: CoreClient) : Params()

        data class GetMessage(val accountId: AccountId) : Params()

        data class Register(val accountId: AccountId, val signature: Model.Signature) : Params()

        data class Create(val accountId: AccountId, val store: Type.Store) : Params()

        data class Set(val accountId: AccountId, val store: Type.Store, val key: String, val value: String) : Params()

        data class Delete(val accountId: AccountId, val store: Type.Store, val key: String) : Params()

        data class GetStores(val accountId: AccountId) : Params()
    }
}