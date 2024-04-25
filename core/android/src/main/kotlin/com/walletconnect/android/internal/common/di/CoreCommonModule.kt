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
            .withSubtype(Props.W3M.ModalCreated::class.java, "modal_created")
            .withSubtype(Props.W3M.ModalLoaded::class.java, "modal_loaded")
            .withSubtype(Props.W3M.ModalOpen::class.java, "modal_open")
            .withSubtype(Props.W3M.ModalClose::class.java, "modal_close")
            .withSubtype(Props.W3M.ClickNetworks::class.java, "click_networks")
            .withSubtype(Props.W3M.ClickAllWallets::class.java, "click_all_wallets")
            .withSubtype(Props.W3M.SwitchNetwork::class.java, "switch_network")
            .withSubtype(Props.W3M.SelectWallet::class.java, "select_wallet")
            .withSubtype(Props.W3M.ConnectSuccess::class.java, "connect_success")
            .withSubtype(Props.W3M.ConnectError::class.java, "connect_error")
            .withSubtype(Props.W3M.DisconnectSuccess::class.java, "disconnect_success")
            .withSubtype(Props.W3M.DisconnectError::class.java, "disconnect_error")
            .withSubtype(Props.W3M.ClickWalletHelp::class.java, "click_wallet_help")
            .withSubtype(Props.W3M.ClickNetworkHelp::class.java, "click_network_help")
            .withSubtype(Props.W3M.ClickGetWallet::class.java, "click_get_wallet")
            .withSubtype(Props.Pairing.MalformedPairingUri::class.java, "malformed_pairing_uri")
            .withSubtype(Props.Pairing.PairingAlreadyExist::class.java, "pairing_already_exist")
            .withSubtype(Props.Pairing.FailedToSubscribeToPairingTopic::class.java, "failed_to_subscribe_to_pairing_topic")
            .withSubtype(Props.Pairing.PairingExpired::class.java, "pairing_expired")
            .withSubtype(Props.NoWSSConnection::class.java, "no_wss_connection")
            .withSubtype(Props.NoInternetConnection::class.java, "no_internet_connection")
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