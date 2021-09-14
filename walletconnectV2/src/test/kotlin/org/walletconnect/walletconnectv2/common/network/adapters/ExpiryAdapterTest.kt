package org.walletconnect.walletconnectv2.common.network.adapters

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.walletconnect.walletconnectv2.common.Expiry

internal class ExpiryAdapterTest {
    private val moshi = Moshi.Builder()
        .add(ExpiryAdapter)
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun toJson() {
        val expiry = Expiry(100L)
        val expected = """"${expiry.seconds}""""

        val expiryJson = moshi.adapter(Expiry::class.java).toJson(expiry)

        Assertions.assertEquals(expected, """"$expiryJson"""")
    }
}