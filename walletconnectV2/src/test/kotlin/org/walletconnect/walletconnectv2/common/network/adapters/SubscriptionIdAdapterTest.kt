package org.walletconnect.walletconnectv2.common.network.adapters

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.jupiter.api.Test
import org.walletconnect.walletconnectv2.common.SubscriptionId
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class SubscriptionIdAdapterTest {
    private val moshi = Moshi.Builder()
        .add { _, _, _ ->
            SubscriptionIdAdapter
        }
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun fromJson() {
        val exampleJson = """
            {
              "id":1,
              "jsonrpc":"2.0",
              "result":"subscriptionId1"
            }
        """.trimIndent()
        val expected = SubscriptionId("subscriptionId1")

        val resultSubscriptionId = moshi.adapter(SubscriptionId::class.java).fromJson(exampleJson)

        assertNotNull(resultSubscriptionId)
        assertEquals(expected, resultSubscriptionId)
    }
}