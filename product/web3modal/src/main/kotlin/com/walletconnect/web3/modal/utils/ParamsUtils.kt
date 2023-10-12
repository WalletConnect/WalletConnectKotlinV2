package com.walletconnect.web3.modal.utils

import com.walletconnect.web3.modal.client.Modal
import org.intellij.lang.annotations.Language

internal fun createAddEthChainParams(chain: Modal.Model.Chain): String {
    val chainHex = chain.chainReference.toInt().toString(radix = 16)

    @Language("JSON")
    val param = """
            [
              {
                "chainId": "0x$chainHex",
                "blockExplorerUrls": [
                  "${chain.blockExplorerUrl}"
                ],
                "chainName": "${chain.chainName}",
                "nativeCurrency": {
                  "name": "${chain.token.name}",
                  "symbol": "${chain.token.symbol}",
                  "decimals": ${chain.token.decimal}
                },
                "rpcUrls": [
                  "${chain.rpcUrl}"
                ]
              }
            ]
        """
    return param.formatParams()
}

internal fun createSwitchChainParams(chain: Modal.Model.Chain): String {
    val chainHex = chain.chainReference.toInt().toString(radix = 16)
    @Language("JSON")
    val param = """
            [
              {
                "chainId": "0x$chainHex"
              }
            ]
        """
    return param.formatParams()
}


internal fun String.formatParams() = this.trimIndent().replace("\n", "").replace(" ", "")
