package com.walletconnect.sign.adapters

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.walletconnect.sign.core.adapters.SubscriptionIdAdapter
import com.walletconnect.sign.core.model.vo.SubscriptionIdVO
import org.junit.jupiter.api.Test
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