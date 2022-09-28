package com.walletconnect.android

import com.walletconnect.android.pairing.PairingInterface
import com.walletconnect.android.common.relay.RelayConnectionInterface

interface CoreInterface : RelayConnectionInterface, PairingInterface