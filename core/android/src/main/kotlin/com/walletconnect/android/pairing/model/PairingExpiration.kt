@file:JvmName("Expiration")

package com.walletconnect.android.pairing.model

import com.walletconnect.android.internal.utils.currentTimeInSeconds
import com.walletconnect.android.internal.utils.fiveMinutesInSeconds

val pairingExpiry: Long get() = currentTimeInSeconds + fiveMinutesInSeconds