package com.walletconnect.android.internal.common.explorer.data.network.model

import androidx.core.net.toUri
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
internal class DappListingsDTOJunit4Test {
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

    @Test
    fun `Uri compareTo of the same uri with no paths`() {
        val uri1 = "https://gm.walletconnect.com".toUri()
        val uri2 = "https://gm.walletconnect.com".toUri()
        assert(uri1.compareTo(uri2) == 0)
    }

    @Test
    fun `Uri compareTo of the same uri with differing paths`() {
        val uri1 = "https://gm.walletconnect.com/path1".toUri()
        val uri2 = "https://gm.walletconnect.com/path2".toUri()
        assert(uri1.compareTo(uri2) != 0)
    }

    @Test
    fun `Comparing uri host of the same uri with differing paths`() {
        val uri1 = assertNotNull("https://gm.walletconnect.com/path1".toUri().host)
        val uri2 = assertNotNull("https://gm.walletconnect.com/path2".toUri().host)
        assert(uri1.contains(uri2))
    }

    @Test
    fun `Uri compareTo of the different uris`() {
        val uri1 = "https://gm.walletconnect.com".toUri()
        val uri2 = "https://pancakeswap.com".toUri()
        assert(uri1.compareTo(uri2) != 0)
    }
}