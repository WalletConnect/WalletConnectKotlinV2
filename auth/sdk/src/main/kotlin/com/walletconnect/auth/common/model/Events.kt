@file:JvmSynthetic

package com.walletconnect.auth.common.model

import com.walletconnect.android.common.model.EngineEvent

internal sealed class Events : EngineEvent {
    data class OnAuthRequest(val id: Long, val message: String) : Events()
    data class OnAuthResponse(val id: Long, val response: AuthResponse) : Events()
}