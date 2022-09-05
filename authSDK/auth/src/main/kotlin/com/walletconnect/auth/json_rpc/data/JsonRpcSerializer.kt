@file:JvmSynthetic

package com.walletconnect.auth.json_rpc.data

import com.squareup.moshi.Moshi
import com.walletconnect.android.impl.common.model.type.ClientParams
import com.walletconnect.android.api.SerializableJsonRpc
import com.walletconnect.android.impl.json_rpc.data.JsonRpcSerializerAbstract

internal class JsonRpcSerializer(override val moshi: Moshi) : JsonRpcSerializerAbstract(moshi) {

    override fun deserialize(method: String, json: String): ClientParams? {
        TODO("Not yet implemented")
    }

    override fun sdkSpecificSerialize(payload: SerializableJsonRpc): String {
        TODO("Not yet implemented")
    }

}