@file:JvmSynthetic

package com.walletconnect.sign.core.scope

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

private val job = SupervisorJob()

@get:JvmSynthetic
internal var scope = CoroutineScope(job + Dispatchers.IO)

