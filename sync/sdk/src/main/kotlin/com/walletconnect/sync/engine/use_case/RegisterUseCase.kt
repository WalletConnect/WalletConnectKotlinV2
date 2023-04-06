package com.walletconnect.sync.engine.use_case

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.signing.signature.Signature

// This one is a class instead of object as it will need storage repository for entropy storage
internal class RegisterUseCase() : RegisterUseCaseInterface, GetMessageUseCaseInterface by GetMessageUseCase {

    override fun register(accountId: AccountId, signature: Signature) {
        val message = getMessage(accountId) // Will be useful later. Wanted to see how other usecases could be put in
        TODO()
    }
}

internal interface RegisterUseCaseInterface {
    fun register(accountId: AccountId, signature: Signature)
}
