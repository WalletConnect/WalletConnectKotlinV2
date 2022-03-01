package com.walletconnect.walletconnectv2.storage.sequence

enum class SequenceStatus {
    PROPOSED, RESPONDED, //Pending
    PRE_SETTLED, ACKNOWLEDGED //Settled
}