package com.walletconnect.android.sync.crypto

import com.walletconnect.android.utils.cacao.CacaoSignerInterface
import com.walletconnect.android.sync.client.Sync

// In Sync there is no cacao, but CacaoSignerInterface has support for EIP191, and EIP1271 signing which is required by sync. 
object MessageSigner : CacaoSignerInterface<Sync.Model.Signature>