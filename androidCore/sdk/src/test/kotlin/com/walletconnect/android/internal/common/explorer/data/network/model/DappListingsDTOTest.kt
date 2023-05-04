package com.walletconnect.android.internal.common.explorer.data.network.model

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.jupiter.api.Test

internal class DappListingsDTOTest {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Test
    fun `serialize listing of all dapps from explorer api`() {
        val json = this.javaClass.classLoader!!.getResourceAsStream("DappListingsSample.json").readBytes().toString(Charsets.UTF_8)
        val adapter = moshi.adapter(DappListingsDTO::class.java)
        val dappListingsDTO = adapter.fromJson(json)

        assert(dappListingsDTO != null)
        assert(dappListingsDTO!!.listings.isNotEmpty())
        assert(dappListingsDTO.listings.size == dappListingsDTO.count)
    }
}