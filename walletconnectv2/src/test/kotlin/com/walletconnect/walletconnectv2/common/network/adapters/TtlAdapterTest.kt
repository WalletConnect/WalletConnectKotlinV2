package com.walletconnect.walletconnectv2.common.network.adapters

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import com.walletconnect.walletconnectv2.common.model.vo.TtlVO
import com.walletconnect.walletconnectv2.common.adapters.TtlAdapter

class TtlAdapterTest {
    private val moshi = Moshi.Builder()
        .add { _, _, _ ->
            TtlAdapter
        }
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun toJson() {
        val ttl = TtlVO(100L)
        val expected = """"${ttl.seconds}""""

        val ttlJson = moshi.adapter(TtlVO::class.java).toJson(ttl)

        Assertions.assertEquals(expected, """"$ttlJson"""")
    }
}