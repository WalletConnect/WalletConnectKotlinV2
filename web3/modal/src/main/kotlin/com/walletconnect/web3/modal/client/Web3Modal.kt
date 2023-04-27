package com.walletconnect.web3.modal.client

import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.web3.modal.di.web3ModalModule
import com.walletconnect.web3.modal.domain.delegate.Web3ModalDelegate



object Web3Modal {
    private var isClientInitialized = false

    //Todo set theme here maybe?
    // add exclude/recommended wallets
    fun initialize(
        init: Modal.Params.Init,
        onError: (Modal.Model.Error) -> Unit
    ) {
        SignClient.initialize(
            init = Sign.Params.Init(init.core),
            onError = { error -> onError(Modal.Model.Error(error.throwable)) }
        )
        runCatching {
            wcKoinApp.modules(
                web3ModalModule()
            )
            SignClient.setDappDelegate(Web3ModalDelegate)
            isClientInitialized = true
        }.onFailure { error -> onError(Modal.Model.Error(error)) }
    }
}