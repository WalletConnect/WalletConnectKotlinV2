package com.walletconnect.sign.common.network.adapters

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.walletconnect.sign.core.adapters.ExpiryAdapter
import com.walletconnect.sign.core.model.vo.ExpiryVO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ExpiryAdapterTest {
    private val moshi = Moshi.Builder()
        .add { _, _, _ ->
            ExpiryAdapter
        }
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun toJson() {
        val expiry = ExpiryVO(100L)
        val expected = """"${expiry.seconds}""""

        val expiryJson = moshi.adapter(ExpiryVO::class.java).toJson(expiry)

        assertEquals(expected, """"$expiryJson"""")
    }
}