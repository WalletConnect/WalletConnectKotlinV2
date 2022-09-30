package com.walletconnect.android

import com.walletconnect.android.pairing.PairingClient
import com.walletconnect.android.pairing.PairingInterface
import com.walletconnect.android.relay.RelayClient
import com.walletconnect.android.relay.RelayConnectionInterface

object CoreClient: RelayConnectionInterface by RelayClient, PairingInterface by PairingClient