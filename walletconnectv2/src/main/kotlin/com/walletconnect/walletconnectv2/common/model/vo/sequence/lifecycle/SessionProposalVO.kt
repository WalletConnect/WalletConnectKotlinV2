package com.walletconnect.walletconnectv2.common.model.vo.sequence.lifecycle

import com.walletconnect.walletconnectv2.common.model.type.SequenceLifecycle
import java.net.URI

data class SessionProposalVO(
    val name: String,
    val description: String,
    val url: String,
    val icons: List<URI>,
    val chains: List<String>,
    val methods: List<String>,
    val types: List<String>,
    val topic: String,
    val proposerPublicKey: String,
    val ttl: Long,
    val accounts: List<String>
) : SequenceLifecycle
