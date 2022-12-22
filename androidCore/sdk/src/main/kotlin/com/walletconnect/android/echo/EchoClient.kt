@file:JvmSynthetic

package com.walletconnect.android.echo

import android.content.SharedPreferences
import com.walletconnect.android.echo.model.EchoBody
import com.walletconnect.android.echo.network.EchoService
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit

internal object EchoClient: EchoInterface {
    private val echoService by lazy { wcKoinApp.koin.get<Retrofit>().create(EchoService::class.java) }
    private const val SUCCESS_STATUS = "SUCCESS"

    override fun initialize() {}

    override fun register(firebaseAccessToken: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        try {
            val clientId = requireNotNull(wcKoinApp.koin.get<SharedPreferences>().getString(EchoInterface.KEY_CLIENT_ID, null))
            val body = EchoBody(clientId, firebaseAccessToken)

            scope.launch(Dispatchers.IO) {
                val response = echoService.register(body)

                if (response.isSuccessful && response.body() != null) {
                    if (response.body()!!.status == SUCCESS_STATUS) {
                        onSuccess()
                    } else {
                        onError(IllegalArgumentException(response.body()!!.errors.first().message))
                    }
                } else {
                    onError(IllegalArgumentException(response.errorBody()?.string()))
                }
            }
        } catch (e: Exception) {
            onError(e)
        }
    }

    override fun unregister() {

    }

    override fun decryptMessage(topic: String, message: String): String {
        // TODO: finish decrypt once Trust fix is merged in
//        val codec = wcKoinApp.koin.get<Codec>
//        return codec.decrypt(topic. message)
        return ""
    }
}