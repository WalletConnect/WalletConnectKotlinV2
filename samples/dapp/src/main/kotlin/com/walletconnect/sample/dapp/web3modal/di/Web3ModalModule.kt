package com.walletconnect.sample.dapp.web3modal.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.walletconnect.android.relay.NetworkClientTimeout
import com.walletconnect.sample.dapp.web3modal.data.explorer.ExplorerMapToListAdapter
import com.walletconnect.sample.dapp.web3modal.data.explorer.ExplorerRepository
import com.walletconnect.sample.dapp.web3modal.domain.usecases.GetWalletsRecommendationsUseCase
import com.walletconnect.sample.dapp.web3modal.domain.usecases.GetWalletsRecommendationsUseCaseImpl
import com.walletconnect.sample.dapp.web3modal.network.ExplorerService
import com.walletconnect.sample.dapp.web3modal.ui.routes.connect_wallet.ConnectYourWalletViewModel
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

fun web3ModalModule() = module {

    val networkClientTimeout = NetworkClientTimeout.getDefaultTimeout()

    //TODO Replace this retrofit, okHttp instance with android core when extract explorerApi from sample

    single {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(ExplorerMapToListAdapter())
            .build()
    }

    single() {
        Retrofit.Builder()
            .baseUrl("https://explorer-api.walletconnect.com/")
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .client(get())
            .build()
    }

    single() {
        OkHttpClient.Builder()
            .writeTimeout(networkClientTimeout.timeout, networkClientTimeout.timeUnit)
            .readTimeout(networkClientTimeout.timeout, networkClientTimeout.timeUnit)
            .callTimeout(networkClientTimeout.timeout, networkClientTimeout.timeUnit)
            .connectTimeout(networkClientTimeout.timeout, networkClientTimeout.timeUnit)
            .build()
    }

    single {
        get<Retrofit>().create(ExplorerService::class.java)
    }

    single { ExplorerRepository(get()) }

    factory<GetWalletsRecommendationsUseCase> { GetWalletsRecommendationsUseCaseImpl(get()) }

    viewModel { ConnectYourWalletViewModel(get()) }

}
