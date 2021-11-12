package org.walletconnect.walletconnectv2

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.utils.getRawType
import org.intellij.lang.annotations.Language
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.walletconnect.walletconnectv2.common.Expiry
import org.walletconnect.walletconnectv2.common.SubscriptionId
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.common.Ttl
import org.walletconnect.walletconnectv2.common.network.adapters.*
import java.util.*

class GenericPayloadsTest {
    private val moshi: Moshi = Moshi.Builder()
        .addLast { type, _, _ ->
            when (type.getRawType().name) {
                Expiry::class.qualifiedName -> ExpiryAdapter
                JSONObject::class.qualifiedName -> JSONObjectAdapter
                SubscriptionId::class.qualifiedName -> SubscriptionIdAdapter
                Topic::class.qualifiedName -> TopicAdapter
                Ttl::class.qualifiedName -> TtlAdapter
                else -> null
            }
        }
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun decodeObject() {
        @Language("JSON")
        val jsonString = """
            {
                "boolKey": true,
                "intKey": 1337,
                "doubleKey": 13.37,
                "stringKey": "verystringwow",
                "objectKey": {
                    "subKey": "0xdeadbeef"
                }
            }
        """.trimIndent()

        val adapter: JsonAdapter<Map<String, Any>> = moshi.adapter(
            Types.newParameterizedType(Map::class.java, String::class.java, Object::class.java))

        val decodedObj = adapter.fromJson(jsonString)
        val obj2 = moshi.adapter(Object::class.java).fromJson(jsonString)
        val obj3 = moshi.adapter(SampleStruct::class.java).fromJson(jsonString)

//        println(adapter.fromJson(jsonString))
//        println(obj2)
        println(obj2 as SampleStruct)
//        println(obj3)
    }

    data class  SampleStruct(
        val boolKey: Boolean,
        val intKey: Int,
        val doubleKey: Double,
        val stringKey: String,
        val objectKey: SubObject
    )

    data class SubObject(val subKey: String)
}