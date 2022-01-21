@file:JvmSynthetic

package com.walletconnect.walletconnectv2.common.scope

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

private val job = SupervisorJob()
internal val scope = CoroutineScope(job + Dispatchers.IO)

