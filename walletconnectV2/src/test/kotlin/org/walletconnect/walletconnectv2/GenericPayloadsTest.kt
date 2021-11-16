package org.walletconnect.walletconnectv2

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GenericPayloadsTest {
    private val moshi: Moshi = Moshi.Builder()
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

        val successfulDecode = decode<SampleStruct>(jsonString)

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