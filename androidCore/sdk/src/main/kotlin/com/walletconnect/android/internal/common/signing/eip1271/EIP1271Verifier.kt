package com.walletconnect.android.internal.common.signing.eip1271

import com.walletconnect.android.internal.common.signing.signature.Signature
import com.walletconnect.android.internal.common.signing.signature.toCacaoSignature
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.generateId
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.web3j.crypto.Sign

internal object EIP1271Verifier {
    private const val isValidSignatureHash = "0x1626ba7e"
    private const val method = "eth_call"
    private const val dynamicTypeOffset = "0000000000000000000000000000000000000000000000000000000000000040"
    private const val dynamicTypeLength = "0000000000000000000000000000000000000000000000000000000000000041"
    private const val mediaTypeString = "application/json; charset=utf-8"
    private const val rpcUrlPrefix = "https://rpc.walletconnect.com/v1/?chainId=eip155:1&projectId="
    private const val hexPrefix = "0x"

    private fun getValidResponse(id: Long): String = "{\"jsonrpc\":\"2.0\",\"id\":$id,\"result\":\"0x1626ba7e00000000000000000000000000000000000000000000000000000000\"}"
    private fun String.prefixWithRpcUrl(): String = rpcUrlPrefix + this

    private fun createBody(to: String, data: String, id: Long): RequestBody {
        val jsonMediaType: MediaType = mediaTypeString.toMediaType()
        val postBody = """{
                |"method" : "$method",
                |"params" : [{"to":"$to", "data":"$data"}, "latest"],
                |"id":${id}, "jsonrpc":"2.0"
                |}""".trimMargin()

        return postBody.toRequestBody(jsonMediaType)
    }

    fun verify(signature: Signature, originalMessage: String, address: String, projectId: String): Boolean {
        return try {
            val messageHash: String = Sign.getEthereumMessageHash(originalMessage.toByteArray()).bytesToHex()
            val data: String = isValidSignatureHash + messageHash + dynamicTypeOffset + dynamicTypeLength + signature.toCacaoSignature().removePrefix(hexPrefix)

            val id = generateId()
            val request: Request = Request.Builder().url(projectId.prefixWithRpcUrl()).post(createBody(address, data, id)).build()
            val response: Response = OkHttpClient().newCall(request).execute()

            val responseString = response.body?.string()
            println(responseString)
            responseString == getValidResponse(id)
        } catch (e: Exception) {
            println(e)
            false
        }
    }
}