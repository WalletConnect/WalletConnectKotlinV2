package com.walletconnect.notify.common.model

import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.foundation.common.model.PrivateKey

internal data class CacaoPayloadWithIdentityPrivateKey(val payload: Cacao.Payload, val key: PrivateKey)
