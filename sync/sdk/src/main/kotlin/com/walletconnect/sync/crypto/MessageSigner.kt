package com.walletconnect.sync.crypto

import com.walletconnect.android.cacao.CacaoSignerInterface
import com.walletconnect.sync.client.Sync

// In Sync there is no cacao, but CacaoSignerInterface has support for EIP191, and EIP1271 signing which is required by sync. 
object MessageSigner : CacaoSignerInterface<Sync.Model.Signature>