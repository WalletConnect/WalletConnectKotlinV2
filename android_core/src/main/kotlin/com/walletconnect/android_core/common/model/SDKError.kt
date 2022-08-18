package com.walletconnect.android_core.common.model

import com.walletconnect.android_core.common.InternalError
import com.walletconnect.android_core.common.model.type.EngineEvent

class SDKError(val exception: InternalError) : EngineEvent
