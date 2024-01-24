@file:JvmName("Expiration")

package com.walletconnect.android.pairing.model

import com.walletconnect.android.internal.utils.currentTimeInSeconds
import com.walletconnect.android.internal.utils.fiveMinutesInSeconds
import com.walletconnect.android.internal.utils.monthInSeconds

val inactivePairing: Long get() = currentTimeInSeconds + fiveMinutesInSeconds
val activePairing: Long get() = currentTimeInSeconds + monthInSeconds