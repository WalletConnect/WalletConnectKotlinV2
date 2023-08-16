package com.walletconnect.android.test.utils

import com.walletconnect.foundation.common.model.PublicKey
import io.ipfs.multibase.Multibase
import org.web3j.crypto.Keys

object EthereumAccount {
    val privKey: String = "69dc814984ae6ac09d6ce8034cd43f6fbabc2b397c56b1d54468e53c45c8e215"
    val pubKey: String = "964033049d706b36e477d6c09c4ef7cd0f92460a146cb5c24f5b10cb5ccdc46300d9dbb4715f79d081bf45af98b055ca6c7a5087be57d9e4992b9eca9646d9a0"
    val caip10: String = "eip155:1:${Keys.toChecksumAddress(Keys.getAddress(pubKey))}"
}

object BNBAccount {
    val privKey: String = "69dc814984ae6ac09d6ce8034cd43f6fbabc2b397c56b1d54468e53c45c8e215"
    val pubKey: String = "964033049d706b36e477d6c09c4ef7cd0f92460a146cb5c24f5b10cb5ccdc46300d9dbb4715f79d081bf45af98b055ca6c7a5087be57d9e4992b9eca9646d9a0"
    val caip10: String = "eip155:56:${Keys.toChecksumAddress(Keys.getAddress(pubKey))}"
}


object SolanaAccount {
    const val privKey: String = "a0f7d46edfc1c52083415d5d66320c503c09d51c258e51d91ed85c6c95670548"
    const val pubKey: String = "d3c8875c064d8076118dda2879b31d54c8083dc41cce80514f5d573b83381c1c"
    val caip10 = "solana:5eykt4UsFv8P8NJdTREpY1vzqKqZKvdp:${Multibase.encode(Multibase.Base.Base58BTC, PublicKey(pubKey).keyAsBytes).takeLast(44)}"
}


