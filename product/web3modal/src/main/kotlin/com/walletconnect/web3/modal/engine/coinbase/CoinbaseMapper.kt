package com.walletconnect.web3.modal.engine.coinbase

import com.coinbase.android.nativesdk.message.request.AddChainNativeCurrency
import com.coinbase.android.nativesdk.message.request.WatchAssetOptions
import com.coinbase.android.nativesdk.message.request.Web3JsonRPC
import com.coinbase.android.nativesdk.message.response.ActionResult
import com.walletconnect.util.Empty
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

internal fun List<ActionResult>.toCoinbaseResults() = map {
    when (it) {
        is ActionResult.Result -> CoinbaseResult.Result(it.value)
        is ActionResult.Error -> CoinbaseResult.Error(it.code, it.message)
    }
}

internal fun String.toPersonalSignCoinbase(): Web3JsonRPC.PersonalSign {
    val jsonArray = parseJsonArray(this)
    return Web3JsonRPC.PersonalSign(jsonArray[1].removeQuotationMarks(), jsonArray[0].removeQuotationMarks())
}

internal fun String.toSignTypedDataV3(): Web3JsonRPC.SignTypedDataV3 {
    val jsonArray = parseJsonArray(this)
    return Web3JsonRPC.SignTypedDataV3(jsonArray[0], jsonArray[1])
}

internal fun String.toSignTypedDataV4(): Web3JsonRPC.SignTypedDataV4 {
    val jsonArray = parseJsonArray(this)
    return Web3JsonRPC.SignTypedDataV4(jsonArray[0], jsonArray[1])
}

internal fun String.toEthSignTransaction(chainReference: String): Web3JsonRPC.SignTransaction {
    val jsonArray = JSONArray(this)
    val jsonObject = jsonArray.optJSONObject(0) ?: throw Throwable("Invalid json object")

    return Web3JsonRPC.SignTransaction(
        fromAddress = jsonObject.getString("from"),
        toAddress = jsonObject.getStringOrNull("to"),
        chainId = chainReference.getChainId(),
        weiValue = jsonObject.getString("value"),
        data = jsonObject.getString("data"),
        nonce = jsonObject.getStringOrNull("nonce")?.hexToInt(),
        gasPriceInWei = jsonObject.getStringOrNull("gasPrice")?.convertHexValueToWeiValue().toString(),
        maxFeePerGas = jsonObject.getStringOrNull("maxFeePerGas"),
        maxPriorityFeePerGas = jsonObject.getStringOrNull("maxPriorityFeePerGas"),
        gasLimit = jsonObject.getStringOrNull("gasLimit"),

    )
}

internal fun String.toEthSendTransaction(chainReference: String): Web3JsonRPC.SendTransaction {
    val jsonArray = JSONArray(this)
    val jsonObject = jsonArray.optJSONObject(0) ?: throw Throwable("Invalid json object")

    return Web3JsonRPC.SendTransaction(
        fromAddress = jsonObject.getString("from"),
        toAddress = jsonObject.getStringOrNull("to"),
        weiValue = jsonObject.getString("value"),
        data = jsonObject.getString("data"),
        nonce = jsonObject.getStringOrNull("nonce")?.hexToInt(),
        gasPriceInWei = jsonObject.getStringOrNull("gasPrice")?.convertHexValueToWeiValue().toString(),
        maxFeePerGas = jsonObject.getStringOrNull("maxFeePerGas"),
        maxPriorityFeePerGas = jsonObject.getStringOrNull("maxPriorityFeePerGas"),
        gasLimit = jsonObject.getStringOrNull("gasLimit"),
        chainId = chainReference.getChainId()
    )
}

internal fun String.toAddEthChain(): Web3JsonRPC.AddEthereumChain {
    val jsonArray = JSONArray(this)
    val jsonObject = jsonArray.optJSONObject(0) ?: throw Throwable("Invalid json object")

    return Web3JsonRPC.AddEthereumChain(
        chainId = jsonObject.optString("chainId").removePrefix(),
        blockExplorerUrls = jsonObject.optJSONArray("blockExplorerUrls")?.toListString(),
        chainName = jsonObject.optString("chainName"),
        iconUrls = jsonObject.optJSONArray("iconUrls")?.toListString(),
        nativeCurrency = jsonObject.optJSONObject("nativeCurrency")?.parseNativeCurrency(),
        rpcUrls = jsonObject.optJSONArray("rpcUrls")?.toListString() ?: emptyList()
    )
}

internal fun String.toSwitchEthChain(): Web3JsonRPC.SwitchEthereumChain {
    val jsonArray = JSONArray(this)
    val jsonObject = jsonArray.optJSONObject(0) ?: throw Throwable("Invalid json object")
    val chainId = jsonObject.optString("chainId").removePrefix()
    return Web3JsonRPC.SwitchEthereumChain(chainId)
}

internal fun String.toWalletWatchAssets(): Web3JsonRPC.WatchAsset {
    val jsonArray = JSONArray(this)
    val jsonObject = jsonArray.optJSONObject(0) ?: throw Throwable("Invalid json object")

    return Web3JsonRPC.WatchAsset(
        type = jsonObject.getString("type"),
        options = WatchAssetOptions(
            address = jsonObject.getString("address"),
            symbol = jsonObject.getStringOrNull("symbol"),
            decimals = jsonObject.getInt("decimals"),
            image = jsonObject.getStringOrNull("image")
        )
    )
}

private fun JSONObject.parseNativeCurrency() = AddChainNativeCurrency(name = optString("name"), symbol = optString("symbol"), decimals = optInt("decimals"))
private fun JSONArray.toListString(): List<String> {
    val list = mutableListOf<String>()
    for (i in 0 until length()) {
        list.add(getString(i))
    }
    return list
}

private fun JSONObject.getStringOrNull(name: String) = try {
    getString(name)
} catch (e: Throwable) {
    null
}

private fun String.removeQuotationMarks() = removeSurrounding("\"")

private fun String.removePrefix() = removePrefix("0x")

private fun parseJsonArray(jsonString: String): List<String> = jsonString.trim('[', ']').split(",").map { it.trim() }

private fun String.convertHexValueToWeiValue() = BigDecimal(removePrefix().toLong(16).toString())

private fun String.getChainId() = split(":").lastOrNull() ?: String.Empty

private fun String.hexToInt() = removePrefix("0x").toBigInteger(16).toInt()
