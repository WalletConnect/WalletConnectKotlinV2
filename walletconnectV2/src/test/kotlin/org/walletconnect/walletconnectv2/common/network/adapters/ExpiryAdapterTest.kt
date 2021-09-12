package org.walletconnect.walletconnectv2.common.network.adapters

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.walletconnect.walletconnectv2.common.Expiry
import kotlin.random.Random

internal class ExpiryAdapterTest {
    private val moshi = Moshi.Builder()
        .add(ExpiryAdapter)
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun fromJson() {
    }

    @Test
    fun toJson() {
        val expiry = Expiry(Random.nextLong())
        val expected = """"${expiry.seconds}""""

        val expiryJson = moshi.adapter(Expiry::class.java).toJson(expiry)

        Assertions.assertEquals(expected, """"$expiryJson"""")
    }
}