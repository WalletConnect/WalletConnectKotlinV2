package com.walletconnect.walletconnectv2.common.network.adapters

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import com.walletconnect.walletconnectv2.common.model.Expiry
import com.walletconnect.walletconnectv2.common.adapters.ExpiryAdapter

internal class ExpiryAdapterTest {
    private val moshi = Moshi.Builder()
        .add { _, _, _ ->
            ExpiryAdapter
        }
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun toJson() {
        val expiry = Expiry(100L)
        val expected = """"${expiry.seconds}""""

        val expiryJson = moshi.adapter(Expiry::class.java).toJson(expiry)

        assertEquals(expected, """"$expiryJson"""")
    }
}