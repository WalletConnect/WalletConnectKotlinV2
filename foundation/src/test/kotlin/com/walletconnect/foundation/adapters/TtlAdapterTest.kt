package com.walletconnect.foundation.adapters

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.walletconnect.foundation.common.adapters.TtlAdapter
import com.walletconnect.foundation.common.model.Ttl
import junit.framework.TestCase.assertEquals
import org.junit.Test

class TtlAdapterTest {
    private val moshi = Moshi.Builder()
        .add { _, _, _ ->
            TtlAdapter
        }
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun toJson() {
        val ttl = Ttl(100L)
        val expected = """"${ttl.seconds}""""

        val ttlJson = moshi.adapter(Ttl::class.java).toJson(ttl)

        assertEquals(expected, """"$ttlJson"""")
    }
}