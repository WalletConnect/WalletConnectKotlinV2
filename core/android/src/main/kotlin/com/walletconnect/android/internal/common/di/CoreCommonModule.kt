package com.walletconnect.android.internal.common.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.adapter.ExpiryAdapter
import com.walletconnect.android.internal.common.adapter.JsonRpcResultAdapter
import com.walletconnect.android.internal.common.adapter.TagsAdapter
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.pulse.model.properties.Props
import com.walletconnect.foundation.di.FoundationDITags
import com.walletconnect.foundation.di.foundationCommonModule
import com.walletconnect.foundation.util.Logger
import org.koin.core.qualifier.named
import org.koin.dsl.module
import timber.log.Timber
import kotlin.reflect.jvm.jvmName

fun coreCommonModule() = module {

    includes(foundationCommonModule())

    single<PolymorphicJsonAdapterFactory<JsonRpcResponse>> {
        PolymorphicJsonAdapterFactory.of(JsonRpcResponse::class.java, "type")
            .withSubtype(JsonRpcResponse.JsonRpcResult::class.java, "result")
            .withSubtype(JsonRpcResponse.JsonRpcError::class.java, "error")
    }

    single<PolymorphicJsonAdapterFactory<Props>> {
        PolymorphicJsonAdapterFactory.of(Props::class.java, "type")
            .withSubtype(Props.ModalCreated::class.java, "modal_created")
            .withSubtype(Props.ModalLoaded::class.java, "modal_loaded")
            .withSubtype(Props.ModalOpen::class.java, "modal_open")
            .withSubtype(Props.ModalClose::class.java, "modal_close")
            .withSubtype(Props.ClickNetworks::class.java, "click_networks")
            .withSubtype(Props.ClickAllWallets::class.java, "click_all_wallets")
            .withSubtype(Props.SwitchNetwork::class.java, "switch_network")
            .withSubtype(Props.SelectWallet::class.java, "select_wallet")
            .withSubtype(Props.ConnectSuccess::class.java, "connect_success")
            .withSubtype(Props.ConnectError::class.java, "connect_error")
            .withSubtype(Props.DisconnectSuccess::class.java, "disconnect_success")
            .withSubtype(Props.DisconnectError::class.java, "disconnect_error")
            .withSubtype(Props.ClickWalletHelp::class.java, "click_wallet_help")
            .withSubtype(Props.ClickNetworkHelp::class.java, "click_network_help")
            .withSubtype(Props.ClickGetWallet::class.java, "click_get_wallet")
    }

    single<Moshi.Builder>(named(AndroidCommonDITags.MOSHI)) {
        get<Moshi>(named(FoundationDITags.MOSHI))
            .newBuilder()
            .add { type, _, moshi ->
                when (type.getRawType().name) {
                    Expiry::class.jvmName -> ExpiryAdapter
                    Tags::class.jvmName -> TagsAdapter
                    JsonRpcResponse.JsonRpcResult::class.jvmName -> JsonRpcResultAdapter(moshi)
                    else -> null
                }
            }
            .add(get<PolymorphicJsonAdapterFactory<JsonRpcResponse>>())
            .add(get<PolymorphicJsonAdapterFactory<Props>>())
    }

    single {
        Timber
    }

    single<Logger>(named(AndroidCommonDITags.LOGGER)) {
        object : Logger {
            override fun log(logMsg: String?) {
                get<Timber.Forest>().d(logMsg)
            }

            override fun log(throwable: Throwable?) {
                get<Timber.Forest>().d(throwable)
            }

            override fun error(errorMsg: String?) {
                get<Timber.Forest>().e(errorMsg)
            }

            override fun error(throwable: Throwable?) {
                get<Timber.Forest>().e(throwable)
            }
        }
    }
}