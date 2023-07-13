@file:JvmSynthetic

package com.walletconnect.auth.common.model

import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.verify.data.model.VerifyContext

internal sealed class Events : EngineEvent {
    data class OnAuthRequest(val id: Long, val pairingTopic: String, val payloadParams: PayloadParams, val verifyContext: VerifyContext) : Events()
    data class OnAuthResponse(val id: Long, val response: AuthResponse) : Events()
}