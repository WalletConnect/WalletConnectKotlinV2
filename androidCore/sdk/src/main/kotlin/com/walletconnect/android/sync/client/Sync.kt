package com.walletconnect.android.sync.client

import androidx.annotation.Keep
import com.walletconnect.android.cacao.SignatureInterface
import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.sync.common.model.Store

object Sync {
    sealed class Model {
        @Keep
        data class Signature(override val t: String, override val s: String, override val m: String? = null) : Model(), SignatureInterface
    }


    sealed class Params {
        data class GetMessage(val accountId: AccountId) : Params()

        data class Register(val accountId: AccountId, val signature: Model.Signature, val signatureType: SignatureType) : Params()

        data class IsRegistered(val accountId: AccountId) : Params()

        data class Create(val accountId: AccountId, val store: Store) : Params()

        data class Set(val accountId: AccountId, val store: Store, val key: String, val value: String) : Params()

        data class Delete(val accountId: AccountId, val store: Store, val key: String) : Params()

        data class GetStores(val accountId: AccountId) : Params()

        data class GetStoreTopics(val accountId: AccountId, val store: String) : Params()
    }
}