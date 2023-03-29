package com.walletconnect.android.verify

import com.walletconnect.android.internal.common.di.verifyModule

internal object VerifyClient : VerifyInterface {

    //todo: maybe init is not needed?
    override fun initialize(verifyUrl: String?) {
        verifyModule(verifyUrl)
    }

    override fun register(attestationId: String) {
        TODO("Not yet implemented")
    }

    override fun resolve(attestationId: String, onSuccess: (String) -> Unit) {
        TODO("Not yet implemented")
    }
}