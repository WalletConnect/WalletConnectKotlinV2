package com.walletconnect.sign.adapters

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GenericPayloadsTest {
    private val moshi: Moshi = Moshi.Builder()
//        .add(SingleToArrayAdapter.INSTANCE)
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


        @Language("JSON")
        val jsonString2 = """
              ["0xdeadbeaf","0x9b2055d370f73ec7d8a03e965129118dc8f5bf83"]
        """.trimIndent()

        val successfulDecode = decode<SampleStruct>(jsonString)
//        println(decode<Any>(jsonString2)) // TODO: For generic adapter, Deserialize into JSONObject/JSONArray and then parse object and generate passed type

        assertNotNull(successfulDecode)
    }

    @Test
    fun decodeInvalidObject() {
        @Language("JSON")
        val invalidJsonString = """
            {
                "boolKey": "invalid test",
                "intKey": 1337,
                "doubleKey": 13.37,
                "stringKey": "verystringwow"
            }
        """.trimIndent()

        val failedDecode = decode<SampleStruct>(invalidJsonString)

        assertNull(failedDecode)
    }

    private inline fun <reified T> decode(jsonString: String): T? {
        return try {
            moshi.adapter(T::class.java).fromJson(jsonString)
        } catch (exception: JsonDataException) {
            null
        }
    }

    // Source: https://stackoverflow.com/questions/53344033/moshi-parse-single-object-or-list-of-objects-kotlin
//    class SingleToArrayAdapter(val delegateAdapter: JsonAdapter<List<Any>>, val elementAdapter: JsonAdapter<Any>) : JsonAdapter<Any>() {
//
//        companion object {
//            val INSTANCE = SingleToArrayAdapterFactory()
//        }
//
//        override fun fromJson(reader: JsonReader): Any? =
//            if (reader.peek() != JsonReader.Token.BEGIN_ARRAY) {
//                Collections.singletonList(elementAdapter.fromJson(reader))
//            } else delegateAdapter.fromJson(reader)
//
//        override fun toJson(writer: JsonWriter, value: Any?) =
//            throw UnsupportedOperationException("SingleToArrayAdapter is only used to deserialize objects")
//
//        class SingleToArrayAdapterFactory : JsonAdapter.Factory {
//            override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<Any>? {
//                val elementType = Types.collectionElementType(type, List::class.java)
//                val delegateAdapter: JsonAdapter<List<Any>> = moshi.adapter(type)
//                val elementAdapter: JsonAdapter<Any> = moshi.adapter(elementType)
//
//                return SingleToArrayAdapter(delegateAdapter, elementAdapter)
//            }
//        }
//    }

    private data class SampleStruct(
        val boolKey: Boolean,
        val intKey: Int,
        val doubleKey: Double,
        val stringKey: String,
        val objectKey: SubObject
    ) {
        data class SubObject(val subKey: String)
    }
}