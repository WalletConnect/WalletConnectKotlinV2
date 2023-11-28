@file:JvmSynthetic

package com.walletconnect.android.internal.common.jwt.clientid

import android.content.SharedPreferences
import androidx.core.content.edit
import com.walletconnect.android.echo.client.EchoInterface
import com.walletconnect.android.utils.strippedUrl
import com.walletconnect.foundation.crypto.data.repository.ClientIdJwtRepository

internal class GenerateJwtStoreClientIdUseCase(private val clientIdJwtRepository: ClientIdJwtRepository, private val sharedPreferences: SharedPreferences) {

    operator fun invoke(relayUrl: String): String =
        clientIdJwtRepository.generateJWT(relayUrl.strippedUrl()) { clientId ->
            sharedPreferences.edit {
                putString(EchoInterface.KEY_CLIENT_ID, clientId)
            }
        }
}