package com.walletconnect.android.internal.common.adapter

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class ExpiryAdapterTest {
    private val moshi = Moshi.Builder()
        .add { _, _, _ ->
            ExpiryAdapter
        }
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun toJson() {
        val expiry = com.walletconnect.android.internal.common.model.Expiry(100L)
        val expected = """"${expiry.seconds}""""

        val expiryJson = moshi.adapter(com.walletconnect.android.internal.common.model.Expiry::class.java).toJson(expiry)

        assertEquals(expected, """"$expiryJson"""")
    }
}