@file:JvmSynthetic

package com.walletconnect.android.api

import android.app.Application
import android.net.Uri
import android.os.Build
import com.walletconnect.android.api.di.*
import com.walletconnect.foundation.crypto.data.repository.JwtRepository
import com.walletconnect.foundation.network.BaseRelayClient
import com.walletconnect.foundation.network.data.ConnectionController
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

//developers should be able to create instance of RelayClient and send messages through the network

class RelayClient(relayServerUrl: String, connectionType: ConnectionType, application: Application) : BaseRelayClient(), RelayConnectionInterface, KoinComponent {

//    val relay: RelayService //by lazy { wcKoinApp.koin.get(named("android_api_relay_service")) }

    init {
        println("kobe; RelayClient INIT")

//        foundationKoinApp.close()

        wcKoinApp.run {
            androidContext(application)
            modules(
                commonModule(),
                androidApiCryptoModule()
            )
        }

        val jwtRepository = wcKoinApp.koin.get<JwtRepository>()
        val jwt = jwtRepository.generateJWT(relayServerUrl.strippedUrl())
        val serverUrl = relayServerUrl.addUserAgent("2.0.0")

//        println("kobe; jwt: $jwt; server uri: $serverUrl")

        wcKoinApp.modules(
//            networkModule(serverUrl, "1111", jwt),
            androidApiNetworkModule(serverUrl, jwt, connectionType, "2.0.0")
        )

        println("kobe; Android Relay Service")
        relay = wcKoinApp.koin.get(named(AndroidApiDITags.RELAY_SERVICE))
        //TODO: how to get sdk version?

        println("kobe; end init relay client")
    }

    private val connectionController: ConnectionController by inject()//= wcKoinApp.koin.get()

    val logger = LoggerImpl()

    override fun connect(onError: (String) -> Unit) {
        when (connectionController) {
            is ConnectionController.Automatic -> onError(WRONG_CONNECTION_TYPE)
            is ConnectionController.Manual -> (connectionController as ConnectionController.Manual).connect()
        }
    }

    override fun disconnect(onError: (String) -> Unit) {
        when (connectionController) {
            is ConnectionController.Automatic -> onError(WRONG_CONNECTION_TYPE)
            is ConnectionController.Manual -> (connectionController as ConnectionController.Manual).disconnect()
        }
    }
}

@JvmSynthetic
internal fun String.strippedUrl() = Uri.parse(this).run {
    this@run.scheme + "://" + this@run.authority
}

@JvmSynthetic
internal fun String.addUserAgent(sdkVersion: String): String {
    return Uri.parse(this).buildUpon()
        // TODO: Setup env variable for version and tag. Use env variable here instead of hard coded version
        .appendQueryParameter("ua", """wc-2/kotlin-$sdkVersion/android-${Build.VERSION.RELEASE}""")
        .build()
        .toString()
}