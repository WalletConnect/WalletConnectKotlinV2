package com.walletconnect.android.internal.common.jwt.clientid

import android.content.SharedPreferences
import com.walletconnect.android.internal.common.di.KEY_CLIENT_ID

class GetClientIdUseCase(private val sharedPreferences: SharedPreferences) {
    operator fun invoke(): String = sharedPreferences.getString(KEY_CLIENT_ID, null) ?: throw IllegalStateException("Client ID not found")
}