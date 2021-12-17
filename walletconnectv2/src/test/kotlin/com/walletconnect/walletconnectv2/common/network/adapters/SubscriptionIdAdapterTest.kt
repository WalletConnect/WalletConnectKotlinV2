package com.walletconnect.walletconnectv2.common.network.adapters

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.jupiter.api.Test
import com.walletconnect.walletconnectv2.common.SubscriptionId
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class SubscriptionIdAdapterTest {
    private val moshi = Moshi.Builder()
        .addLast { _, _, _ ->
            SubscriptionIdAdapter
        }
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun fromJson() {
        val expectedSubscriptionId = "subscriptionId1"
        val expected = SubscriptionId(expectedSubscriptionId)

        val resultSubscriptionId = moshi.adapter(SubscriptionId::class.java).fromJson(expectedSubscriptionId)

        assertNotNull(resultSubscriptionId)
        assertEquals(expected, resultSubscriptionId)
    }
}