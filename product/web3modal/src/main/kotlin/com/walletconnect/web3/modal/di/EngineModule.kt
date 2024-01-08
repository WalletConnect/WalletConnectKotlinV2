package com.walletconnect.web3.modal.di

import com.walletconnect.web3.modal.engine.Web3ModalEngine
import com.walletconnect.web3.modal.engine.coinbase.CoinbaseClient
import org.koin.dsl.module

internal fun engineModule() = module {

    single { Web3ModalEngine(get(), get(), get()) }
    single { CoinbaseClient(get(), get()) }

}
