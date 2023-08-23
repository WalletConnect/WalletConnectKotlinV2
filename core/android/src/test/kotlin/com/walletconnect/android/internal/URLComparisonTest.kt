package com.walletconnect.android.internal

import junit.framework.TestCase.assertFalse
import org.junit.Test
import java.net.URI

class URLComparisonTest {

    private fun compareURIs(metadataUrl: String, originUrl: String): Boolean {
        return when {
            //Check Test6 and Test7
            !metadataUrl.contains("www.") && originUrl.contains("www.") -> URI(metadataUrl).host == URI(originUrl.replace("www.", "")).host
            metadataUrl.contains("www.") && !originUrl.contains("www.") -> URI(metadataUrl.replace("www.", "")).host == URI(originUrl).host
            else -> URI(metadataUrl).host == URI(originUrl).host
        }
    }

    @Test
    fun compareUrlsTest1() {
        val result = compareURIs(metadataUrl = "https://www.known-url.com/", originUrl = "https://www.known-url.com")
        assert(result)
    }

    @Test
    fun compareUrlsTest2() {
        val result = compareURIs(metadataUrl = "https://www.known-url.com/", originUrl = "https://www.known-url.com/subdomain/subdomain2")
        assert(result)
    }

    @Test
    fun compareUrlsTest3() {
        val result = compareURIs(metadataUrl = "https://www.known-url.com/subdomain3", originUrl = "https://www.known-url.com/subdomain/subdomain2")
        assert(result)
    }


    @Test
    fun compareUrlsTest4() {
        val result = compareURIs(metadataUrl = "http://www.known-url.com", originUrl = "https://www.known-url.com/")
        assert(result)
    }

    //Should we add a validation for metadata URI on the SDK side? To allow URIs only with http:// and https://
    @Test
    fun compareUrlsTest5() {
        val result = compareURIs(metadataUrl = "www.known-url.com", originUrl = "https://www.known-url.com/")
        assertFalse(result)
    }

    @Test
    fun compareUrlsTest6() {
        val result = compareURIs(metadataUrl = "https://known-url.com", originUrl = "https://www.known-url.com/")
        assert(result)
    }

    @Test
    fun compareUrlsTest7() {
        val result = compareURIs(metadataUrl = "https://www.known-url.com", originUrl = "https://known-url.com/")
        assert(result)
    }
}