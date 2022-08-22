package com.walletconnect.responder.domain

import com.walletconnect.sample_common.Chains


const val ACCOUNTS_1_ID = 1
val mapOfAccounts1: Map<Chains, String> = mapOf(
    Chains.ETHEREUM_MAIN to "0x022c0c42a80bd19EA4cF0F94c4F9F96645759716",
)

const val ACCOUNTS_2_ID = 2
val mapOfAccounts2: Map<Chains, String> = mapOf(
    Chains.ETHEREUM_MAIN to "0x5496858C1f2f469Eb6A6D378C332e7a4E1dc1B4D",
)

val mapOfAllAccounts = mapOf(
    ACCOUNTS_1_ID to mapOfAccounts1,
    ACCOUNTS_2_ID to mapOfAccounts2
)