@file:JvmSynthetic

package com.walletconnect.android.internal.common.jwt

import android.content.SharedPreferences
import androidx.core.content.edit
import com.walletconnect.android.echo.EchoInterface
import com.walletconnect.android.utils.strippedUrl
import com.walletconnect.foundation.crypto.data.repository.JwtRepository

internal class GenerateJwtStoreClientId(private val jwtRepository: JwtRepository, private val sharedPreferences: SharedPreferences) {

    operator fun invoke(relayUrl: String): String =
        jwtRepository.generateJWT(relayUrl.strippedUrl()) { clientId ->
            sharedPreferences.edit {
                putString(EchoInterface.KEY_CLIENT_ID, clientId)
            }
        }
}