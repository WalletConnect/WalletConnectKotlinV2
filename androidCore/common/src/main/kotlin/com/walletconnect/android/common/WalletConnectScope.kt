package com.walletconnect.android.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

private val job = SupervisorJob()

@get:JvmSynthetic
var scope = CoroutineScope(job + Dispatchers.IO)