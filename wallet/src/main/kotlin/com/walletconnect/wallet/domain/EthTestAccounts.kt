package com.walletconnect.wallet.domain

import com.walletconnect.sample_common.EthChains

const val ACCOUNTS_1_ID = 1
val mapOfAccounts1: Map<EthChains, String> = mapOf(
    EthChains.ETHEREUM_MAIN to "0x022c0c42a80bd19EA4cF0F94c4F9F96645759716",
    EthChains.POLYGON_MATIC to "0x022c0c42a80bd19EA4cF0F94c4F9F96645759716",
    EthChains.ETHEREUM_KOVAN to "0x022c0c42a80bd19EA4cF0F94c4F9F96645759716",
    EthChains.OPTIMISM_KOVAN to "0xf5de760f2e916647fd766b4ad9e85ff943ce3a2b",
    EthChains.POLYGON_MUMBAI to "0x5A9D8a83fF2a032123954174280Af60B6fa32781",
    EthChains.ARBITRUM_RINKBY to "0x682570add15588df8c3506eef2e737db29266de2",
    EthChains.CELO_ALFAJORES to "0xdD5Cb02066fde415dda4f04EE53fBb652066afEE"
)

const val ACCOUNTS_2_ID = 2
val mapOfAccounts2: Map<EthChains, String> = mapOf(
    EthChains.ETHEREUM_MAIN to "0x5496858C1f2f469Eb6A6D378C332e7a4E1dc1B4D",
    EthChains.POLYGON_MATIC to "0x5496858C1f2f469Eb6A6D378C332e7a4E1dc1B4D",
    EthChains.ETHEREUM_KOVAN to "0x5496858C1f2f469Eb6A6D378C332e7a4E1dc1B4D",
    EthChains.OPTIMISM_KOVAN to "0xe16821547bb816ea3f25c67c15a634b104695a32",
    EthChains.POLYGON_MUMBAI to "0x5496858C1f2f469Eb6A6D378C332e7a4E1dc1B4D",
    EthChains.ARBITRUM_RINKBY to "0x49d07a0e25d3d1881bfd1545bb9b12ac2eb00f12",
    EthChains.CELO_ALFAJORES to "0x3a20c11fa54dfbb73f907e3953684a5d93e719a7"
)

val mapOfAllAccounts = mapOf(
    ACCOUNTS_1_ID to mapOfAccounts1,
    ACCOUNTS_2_ID to mapOfAccounts2
)