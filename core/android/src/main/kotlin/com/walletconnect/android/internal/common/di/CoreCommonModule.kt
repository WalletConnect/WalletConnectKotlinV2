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
import com.walletconnect.android.pulse.model.EventType
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
			.withSubtype(Props.Track.ModalCreated::class.java, EventType.Track.MODAL_CREATED)
			.withSubtype(Props.Track.ModalLoaded::class.java, EventType.Track.MODAL_LOADED)
			.withSubtype(Props.Track.ModalOpen::class.java, EventType.Track.MODAL_OPEN)
			.withSubtype(Props.Track.ModalClose::class.java, EventType.Track.MODAL_CLOSE)
			.withSubtype(Props.Track.ClickNetworks::class.java, EventType.Track.CLICK_NETWORKS)
			.withSubtype(Props.Track.ClickAllWallets::class.java, EventType.Track.CLICK_ALL_WALLETS)
			.withSubtype(Props.Track.SwitchNetwork::class.java, EventType.Track.SWITCH_NETWORK)
			.withSubtype(Props.Track.SelectWallet::class.java, EventType.Track.SELECT_WALLET)
			.withSubtype(Props.Track.ConnectSuccess::class.java, EventType.Track.CONNECT_SUCCESS)
			.withSubtype(Props.Track.ConnectError::class.java, EventType.Track.CONNECT_ERROR)
			.withSubtype(Props.Track.DisconnectSuccess::class.java, EventType.Track.DISCONNECT_SUCCESS)
			.withSubtype(Props.Track.DisconnectError::class.java, EventType.Track.DISCONNECT_ERROR)
			.withSubtype(Props.Track.ClickWalletHelp::class.java, EventType.Track.CLICK_WALLET_HELP)
			.withSubtype(Props.Track.ClickNetworkHelp::class.java, EventType.Track.CLICK_NETWORK_HELP)
			.withSubtype(Props.Track.ClickGetWallet::class.java, EventType.Track.CLICK_GET_WALLET)
			.withSubtype(Props.Error.MalformedPairingUri::class.java, EventType.Error.MALFORMED_PAIRING_URI)
			.withSubtype(Props.Error.PairingAlreadyExist::class.java, EventType.Error.PAIRING_ALREADY_EXIST)
			.withSubtype(Props.Error.PairingSubscriptionFailure::class.java, EventType.Error.PAIRING_SUBSCRIPTION_FAILURE)
			.withSubtype(Props.Error.PairingExpired::class.java, EventType.Error.PAIRING_EXPIRED)
			.withSubtype(Props.Error.NoWSSConnection::class.java, EventType.Error.NO_WSS_CONNECTION)
			.withSubtype(Props.Error.NoInternetConnection::class.java, EventType.Error.NO_INTERNET_CONNECTION)
			.withSubtype(Props.Error.ProposalExpired::class.java, EventType.Error.PROPOSAL_EXPIRED)
			.withSubtype(Props.Error.SessionSettlePublishFailure::class.java, EventType.Error.SESSION_SETTLE_PUBLISH_FAILURE)
			.withSubtype(Props.Error.SessionApprovePublishFailure::class.java, EventType.Error.SESSION_APPROVE_PUBLISH_FAILURE)
			.withSubtype(Props.Error.SessionSubscriptionFailure::class.java, EventType.Error.SESSION_SUBSCRIPTION_FAILURE)
			.withSubtype(Props.Error.SessionApproveNamespaceValidationFailure::class.java, EventType.Error.SESSION_APPROVE_NAMESPACE_VALIDATION_FAILURE)
			.withSubtype(Props.Error.RequiredNamespaceValidationFailure::class.java, EventType.Error.REQUIRED_NAMESPACE_VALIDATION_FAILURE)
			.withSubtype(Props.Error.OptionalNamespaceValidationFailure::class.java, EventType.Error.OPTIONAL_NAMESPACE_VALIDATION_FAILURE)
			.withSubtype(Props.Error.SessionPropertiesValidationFailure::class.java, EventType.Error.SESSION_PROPERTIES_VALIDATION_FAILURE)
			.withSubtype(
				Props.Error.AuthenticatedSessionApprovePublishFailure::class.java,
				EventType.Error.AUTHENTICATED_SESSION_APPROVE_PUBLISH_FAILURE
			)
			.withSubtype(Props.Error.MissingSessionAuthenticateRequest::class.java, EventType.Error.MISSING_SESSION_AUTH_REQUEST)
			.withSubtype(Props.Error.SessionAuthenticateRequestExpired::class.java, EventType.Error.SESSION_AUTH_REQUEST_EXPIRED)
			.withSubtype(Props.Error.InvalidCacao::class.java, EventType.Error.INVALID_CACAO)
			.withSubtype(Props.Error.ChainsCaip2CompliantFailure::class.java, EventType.Error.CHAINS_CAIP2_COMPLIANT_FAILURE)
			.withSubtype(Props.Error.ChainsEvmCompliantFailure::class.java, EventType.Error.CHAINS_EVM_COMPLIANT_FAILURE)
			.withSubtype(
				Props.Error.SubscribeAuthenticatedSessionTopicFailure::class.java,
				EventType.Error.SUBSCRIBE_AUTH_SESSION_TOPIC_FAILURE
			)
			.withSubtype(Props.Error.AuthenticatedSessionExpired::class.java, EventType.Error.AUTHENTICATED_SESSION_EXPIRED)
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