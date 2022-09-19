@file:JvmSynthetic

package com.walletconnect.chat.copiedFromSign.core.scope

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

// TODO: This should be extracted to core

private val job = SupervisorJob()

@get:JvmSynthetic
internal var scope = CoroutineScope(job + Dispatchers.IO)

