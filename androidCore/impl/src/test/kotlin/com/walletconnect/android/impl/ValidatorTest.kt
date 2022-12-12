package com.walletconnect.android.impl

import com.walletconnect.android.impl.utils.CoreValidator
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ValidatorTest {
    @Test
    fun `validate chain id against CAIP2 standard`() {
        CoreValidator.isChainIdCAIP2Compliant("").apply { assertEquals(this, false) }
        CoreValidator.isChainIdCAIP2Compliant("bip122:000000000019d6689c085ae165831e93").apply { assertEquals(this, true) }
        CoreValidator.isChainIdCAIP2Compliant("cosmos:cosmoshub-2").apply { assertEquals(this, true) }
        CoreValidator.isChainIdCAIP2Compliant("chainstd:23-33").apply { assertEquals(this, true) }
        CoreValidator.isChainIdCAIP2Compliant("chainasdasdasdasdasdasdasdsastd:23-33").apply { assertEquals(this, false) }
        CoreValidator.isChainIdCAIP2Compliant("cosmoscosmoshub-2").apply { assertEquals(this, false) }
        CoreValidator.isChainIdCAIP2Compliant(":cosmoshub-2").apply { assertEquals(this, false) }
        CoreValidator.isChainIdCAIP2Compliant("cosmos:").apply { assertEquals(this, false) }
        CoreValidator.isChainIdCAIP2Compliant(":").apply { assertEquals(this, false) }
        CoreValidator.isChainIdCAIP2Compliant("123:123").apply { assertEquals(this, true) }
    }

    @Test
    fun `is chain id valid test`() {
        CoreValidator.isAccountIdCAIP10Compliant("").apply { assertEquals(this, false) }
        CoreValidator.isAccountIdCAIP10Compliant("1231:dadd").apply { assertEquals(this, false) }
        CoreValidator.isAccountIdCAIP10Compliant("0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb@eip155:1").apply { assertEquals(this, false) }
        CoreValidator.isAccountIdCAIP10Compliant("polkadot:b0a8d493285c2df73290dfb7e61f870f:5hmuyxw9xdgbpptgypokw4thfyoe3ryenebr381z9iaegmfy")
            .apply { assertEquals(this, true) }
        CoreValidator.isAccountIdCAIP10Compliant("polkadot:b0a8d493285c2df73290dfb7e61f870f:5hmuy:xw9xdgbpptgypokw4thfyoe3ryenebr381z9iaegmfy")
            .apply { assertEquals(this, false) }
        CoreValidator.isAccountIdCAIP10Compliant("polkadotb0a8d493285c2df73290dfb7e61f870f:b0a8d493285c2df73290dfb7e61f870f:5hmuy:xw9xdgbpptgypokw4thfyoe3ryenebr381z9iaegmfy")
            .apply { assertEquals(this, false) }
        CoreValidator.isAccountIdCAIP10Compliant("::").apply { assertEquals(this, false) }
        CoreValidator.isAccountIdCAIP10Compliant("a:s:d").apply { assertEquals(this, false) }
        CoreValidator.isAccountIdCAIP10Compliant("a:s").apply { assertEquals(this, false) }
        CoreValidator.isAccountIdCAIP10Compliant("chainstd:8c3444cf8970a9e41a706fab93e337a6c4:6d9b0b4b9994e8a6afbd3dc3ed983cd51c755afb27cd1dc7825ef59c134a39f7")
            .apply { assertEquals(this, false) }
    }
}