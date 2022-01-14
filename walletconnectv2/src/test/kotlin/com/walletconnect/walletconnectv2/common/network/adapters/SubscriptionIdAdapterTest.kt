package com.walletconnect.walletconnectv2.common.network.adapters

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.jupiter.api.Test
import com.walletconnect.walletconnectv2.common.model.vo.SubscriptionIdVO
import com.walletconnect.walletconnectv2.common.adapters.SubscriptionIdAdapter
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
        val expected = SubscriptionIdVO(expectedSubscriptionId)

        val resultSubscriptionId = moshi.adapter(SubscriptionIdVO::class.java).fromJson(expectedSubscriptionId)

        assertNotNull(resultSubscriptionId)
        assertEquals(expected, resultSubscriptionId)
    }
}