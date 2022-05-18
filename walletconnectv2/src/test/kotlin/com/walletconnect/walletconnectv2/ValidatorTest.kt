package com.walletconnect.walletconnectv2

import com.walletconnect.walletconnectv2.core.exceptions.client.EMPTY_ACCOUNT_LIST_MESSAGE
import com.walletconnect.walletconnectv2.core.exceptions.client.INVALID_EXTEND_TIME
import com.walletconnect.walletconnectv2.core.exceptions.client.UNAUTHORIZED_CHAIN_ID_MESSAGE
import com.walletconnect.walletconnectv2.core.model.vo.SecretKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.engine.domain.Validator
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.engine.model.mapper.toAbsoluteString
import com.walletconnect.walletconnectv2.util.Time
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ValidatorTest {

//    @Test
//    fun `check correct error message when methods are empty `() {
//        val methods: List<String> = listOf()
//
//        Validator.isMethodListNotEmpty(methods) { errorMessage ->
//            assertEquals(EMPTY_RPC_METHODS_LIST_MESSAGE, errorMessage)
//        }
//    }
//
//    @Test
//    fun `check correct error message when events are empty `() {
//        val events: List<String> = listOf()
//
//        Validator.isEventListNotEmpty(events) { errorMessage ->
//            assertEquals(INVALID_EVENTS_MESSAGE, errorMessage)
//        }
//    }
//
//    @Test
//    fun `check if chainIds list is empty`() {
//        val namespaces = listOf(EngineDO.Namespace(chains = listOf(), listOf(), listOf()))
//        Validator.validateCAIP2(namespaces) { errorMessage ->
//            assertEquals(EMPTY_CHAIN_LIST_MESSAGE, errorMessage)
//        }
//    }
//
//    @Test
//    fun `check if chainIds list is invalid`() {
//        val namespaces = listOf(EngineDO.Namespace(chains = listOf("chainID"), listOf(), listOf()))
//        Validator.validateCAIP2(namespaces) { errorMessage ->
//            assertEquals(WRONG_CHAIN_ID_FORMAT_MESSAGE, errorMessage)
//        }
//    }
//
//    @Test
//    fun `validate method`() {
//        val namespaces = listOf(NamespaceVO(methods = listOf("method"), chains = listOf(), events = listOf()))
//        Validator.validateMethodAuthorisation(namespaces, "method1") { errorMessage ->
//            assertEquals(UNAUTHORIZED_METHOD, errorMessage)
//        }
//    }
//
//    @Test
//    fun `are methods valid`() {
//        var methods: List<String> = listOf()
//
//        val result1 = Validator.areMethodsValid(methods)
//        assertEquals(result1, false)
//
//        methods = listOf("", "")
//        val result2 = Validator.areMethodsValid(methods)
//        assertEquals(result2, false)
//
//        methods = listOf("personal_sign", "eth_sign")
//        val result3 = Validator.areMethodsValid(methods)
//        assertEquals(result3, true)
//    }
//
//    @Test
//    fun `are chains permissions not empty`() {
//        var chains: List<String> = listOf()
//
//        val result1 = Validator.isChainListNotEmpty(chains)
//        assertEquals(result1, false)
//
//        chains = listOf("", "")
//        val result2 = Validator.isChainListNotEmpty(chains)
//        assertEquals(result2, false)
//
//        chains = listOf("1", "2")
//        val result3 = Validator.isChainListNotEmpty(chains)
//        assertEquals(result3, true)
//    }
//
//    @Test
//    fun `are events permissions valid`() {
//        var events: List<String> = listOf()
//
//        val result1 = Validator.areEventsValid(events)
//        assertEquals(result1, false)
//
//        events = listOf("", "")
//        val result2 = Validator.areEventsValid(events)
//        assertEquals(result2, false)
//
//        events = listOf("1", "2")
//        val result3 = Validator.areEventsValid(events)
//        assertEquals(result3, true)
//    }

    @Test
    fun `validate chain id against CAIP2 standard`() {
        Validator.isChainIdValid("").apply { assertEquals(this, false) }
        Validator.isChainIdValid("bip122:000000000019d6689c085ae165831e93").apply { assertEquals(this, true) }
        Validator.isChainIdValid("cosmos:cosmoshub-2").apply { assertEquals(this, true) }
        Validator.isChainIdValid("chainstd:23-33").apply { assertEquals(this, true) }
        Validator.isChainIdValid("chainasdasdasdasdasdasdasdsastd:23-33").apply { assertEquals(this, false) }
        Validator.isChainIdValid("cosmoscosmoshub-2").apply { assertEquals(this, false) }
        Validator.isChainIdValid(":cosmoshub-2").apply { assertEquals(this, false) }
        Validator.isChainIdValid("cosmos:").apply { assertEquals(this, false) }
        Validator.isChainIdValid(":").apply { assertEquals(this, false) }
        Validator.isChainIdValid("123:123").apply { assertEquals(this, true) }
    }


    @Test
    fun `check correct error message when accounts are empty and chainIds exists`() {
        val accounts = listOf("")
        val chains = listOf("as", "bc")
        Validator.validateIfAccountsAreOnValidNetwork(accounts, chains) { errorMessage ->
            assertEquals(errorMessage, UNAUTHORIZED_CHAIN_ID_MESSAGE)
        }
    }

    @Test
    fun `check correct error message when accounts are invalid`() {
        val accounts = listOf("as11", "1234")
        val chains = listOf("")
        Validator.validateIfAccountsAreOnValidNetwork(accounts, chains) { errorMessage ->
            assertEquals(errorMessage, UNAUTHORIZED_CHAIN_ID_MESSAGE)
        }
    }

    @Test
    fun `check correct error message when accounts are empty`() {
        val accounts = listOf("", "")

        Validator.validateCAIP10(accounts) { errorMessage ->
            assertEquals(errorMessage, EMPTY_ACCOUNT_LIST_MESSAGE)
        }
    }

    @Test
    fun `check correct error message when accounts are unauthorized`() {
        val accounts = listOf(
            "bip122:000000000019d6689c085ae165831e93:128Lkh3S7CkDTBZ8W7BbpsN3YYizJMp8p6",
            "polkadot:b0a8d493285c2df73290dfb7e61f870f:5hmuyxw9xdgbpptgypokw4thfyoe3ryenebr381z9iaegmfy"
        )
        val chains = listOf("")
        Validator.validateIfAccountsAreOnValidNetwork(accounts, chains) { errorMessage ->
            assertEquals(errorMessage, UNAUTHORIZED_CHAIN_ID_MESSAGE)
        }
    }

    @Test
    fun `are accounts not empty test`() {
        val result1 = Validator.areAccountsNotEmpty(emptyList())
        assertEquals(result1, false)

        val result2 = Validator.areAccountsNotEmpty(listOf(""))
        assertEquals(result2, false)

        val result3 = Validator.areAccountsNotEmpty(listOf("123"))
        assertEquals(result3, true)
    }

    @Test
    fun `is chain id valid test`() {
        Validator.isAccountIdValid("").apply { assertEquals(this, false) }
        Validator.isAccountIdValid("1231:dadd").apply { assertEquals(this, false) }
        Validator.isAccountIdValid("0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb@eip155:1").apply { assertEquals(this, false) }
        Validator.isAccountIdValid("polkadot:b0a8d493285c2df73290dfb7e61f870f:5hmuyxw9xdgbpptgypokw4thfyoe3ryenebr381z9iaegmfy")
            .apply { assertEquals(this, true) }
        Validator.isAccountIdValid("polkadot:b0a8d493285c2df73290dfb7e61f870f:5hmuy:xw9xdgbpptgypokw4thfyoe3ryenebr381z9iaegmfy")
            .apply { assertEquals(this, false) }
        Validator.isAccountIdValid("polkadotb0a8d493285c2df73290dfb7e61f870f:b0a8d493285c2df73290dfb7e61f870f:5hmuy:xw9xdgbpptgypokw4thfyoe3ryenebr381z9iaegmfy")
            .apply { assertEquals(this, false) }
        Validator.isAccountIdValid("::").apply { assertEquals(this, false) }
        Validator.isAccountIdValid("a:s:d").apply { assertEquals(this, false) }
        Validator.isAccountIdValid("a:s").apply { assertEquals(this, false) }
        Validator.isAccountIdValid("chainstd:8c3444cf8970a9e41a706fab93e337a6c4:6d9b0b4b9994e8a6afbd3dc3ed983cd51c755afb27cd1dc7825ef59c134a39f7")
            .apply { assertEquals(this, false) }
    }

    @Test
    fun `are chain ids included in permissions`() {
        Validator.areAccountsOnValidNetworks(emptyList(), emptyList()).apply { assertEquals(this, false) }
        Validator.areAccountsOnValidNetworks(listOf(""), listOf("")).apply { assertEquals(this, false) }
        Validator.areAccountsOnValidNetworks(listOf("as"), listOf("")).apply { assertEquals(this, false) }
        Validator.areAccountsOnValidNetworks(listOf("as"), listOf("ss")).apply { assertEquals(this, false) }
        Validator.areAccountsOnValidNetworks(
            listOf(
                "bip122:000000000019d6689c085ae165831e93:128Lkh3S7CkDTBZ8W7BbpsN3YYizJMp8p6",
                "polkadot:b0a8d493285c2df73290dfb7e61f870f:5hmuyxw9xdgbpptgypokw4thfyoe3ryenebr381z9iaegmfy"
            ), listOf("ss", "aa")
        ).apply { assertEquals(this, false) }

        Validator.areAccountsOnValidNetworks(
            listOf(
                "bip122:128Lkh3S7CkDTBZ8W7BbpsN3YYizJMp8p6",
                "polkadot:5hmuyxw9xdgbpptgypokw4thfyoe3ryenebr381z9iaegmfy"
            ), listOf("bip122:000000000019d6689c085ae165831e93", "polkadot:b0a8d493285c2df73290dfb7e61f870f")
        ).apply { assertEquals(this, false) }

        Validator.areAccountsOnValidNetworks(
            listOf(
                "bip122:000000000019d6689c085ae165831e93:128Lkh3S7CkDTBZ8W7BbpsN3YYizJMp8p6",
                "polkadot:b0a8d493285c2df73290dfb7e61f870f:5hmuyxw9xdgbpptgypokw4thfyoe3ryenebr381z9iaegmfy"
            ), listOf("bip122:000000000019d6689c085ae165831e93", "polkadot:b0a8d493285c2df73290dfb7e61f870f")
        ).apply { assertEquals(this, true) }

        Validator.areAccountsOnValidNetworks(
            listOf(
                "bip122:000000000019d6689c085ae165831e93:128Lkh3S7CkDTBZ8W7BbpsN3YYizJMp8p6",
                "polkadot:b0a8d493285c2df73290dfb7e61f870f:5hmuyxw9xdgbpptgypokw4thfyoe3ryenebr381z9iaegmfy"
            ), listOf("polkadot:b0a8d493285c2df73290dfb7e61f870f")
        ).apply { assertEquals(this, false) }

        Validator.areAccountsOnValidNetworks(
            listOf(
                "bip122:000000000019d6689c085ae165831e93:128Lkh3S7CkDTBZ8W7BbpsN3YYizJMp8p6"
            ), listOf("polkadot:b0a8d493285c2df73290dfb7e61f870f")
        ).apply { assertEquals(this, false) }

        Validator.areAccountsOnValidNetworks(
            listOf(
                "bip122:000000000019d6689c085ae165831e93:128Lkh3S7CkDTBZ8W7BbpsN3YYizJMp8p6"
            ), listOf("bip122:000000000019d6689c085ae165831e93")
        ).apply { assertEquals(this, true) }
    }

//    @Test
//    fun `is chain id authorize test`() {
//        Validator.validateChainIdAuthorization(
//            "bip122:000000000019d6689c085ae165831e93",
//            listOf()
//        ) { assertEquals(UNAUTHORIZED_CHAIN_ID_MESSAGE, it) }
//
//        Validator.validateChainIdAuthorization(
//            "",
//            listOf(NamespaceVO(chains = listOf("polkadot:b0a8d493285c2df73290dfb7e61f870f:"), methods = listOf(), events = listOf()))
//        ) {
//            assertEquals(UNAUTHORIZED_CHAIN_ID_MESSAGE, it)
//        }
//
//        Validator.validateChainIdAuthorization(
//            "bip122:000000000019d6689c085ae165831e93",
//            listOf(NamespaceVO(chains = listOf("polkadot:b0a8d493285c2df73290dfb7e61f870f, bip122:000000000019d6689c085ae165831e93"),
//                events = listOf(),
//                methods = listOf()))
//        ) {
//            assertEquals(UNAUTHORIZED_CHAIN_ID_MESSAGE, it)
//        }
//    }

//    @Test
//    fun `is event valid test`() {
//        var event = EngineDO.Event("", "data", null)
//        Validator.validateEvent(event) {
//            assertEquals(INVALID_EVENT_MESSAGE, it)
//        }
//
//        event = EngineDO.Event("type", "", "")
//        Validator.validateEvent(event) {
//            assertEquals(INVALID_EVENT_MESSAGE, it)
//        }
//
//        event = EngineDO.Event("", "", "")
//        Validator.validateEvent(event) {
//            assertEquals(INVALID_EVENT_MESSAGE, it)
//        }
//    }

    @Test
    fun `validate WC uri test`() {
        val validUri =
            "wc:7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9@2?relay-protocol=waku&symKey=587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303"

        Validator.validateWCUri("").apply { assertEquals(null, this) }
        Validator.validateWCUri(validUri).apply {
            assertNotNull(this)
            assertEquals("7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9", this.topic.value)
            assertEquals("waku", this.relay.protocol)
            assertEquals("587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303", this.symKey.keyAsHex)
            assertEquals("2", this.version)
        }

        val noTopicInvalidUri =
            "wc:@2?relay-protocol=waku&symKey=587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303"
        Validator.validateWCUri(noTopicInvalidUri).apply { assertNull(this) }

        val noPrefixInvalidUri =
            "7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9@2?relay-protocol=waku&symKey=587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303"
        Validator.validateWCUri(noPrefixInvalidUri).apply { assertNull(this) }

        val noSymKeyInvalidUri =
            "wc:7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9@2?relay-protocol=waku&symKey="
        Validator.validateWCUri(noSymKeyInvalidUri).apply { assertNull(this) }

        val noProtocolTypeInvalidUri =
            "wc:7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9@2?relay-protocol=&symKey=587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303"
        Validator.validateWCUri(noProtocolTypeInvalidUri).apply { assertNull(this) }
    }

    @Test
    fun `validate WC uri test optional data field`() {
        val validUri =
            "wc:7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9@2?relay-protocol=waku&relay-data=testData&symKey=587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303"

        Validator.validateWCUri("").apply { assertEquals(null, this) }
        Validator.validateWCUri(validUri).apply {
            assertNotNull(this)
            assertEquals("7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9", this.topic.value)
            assertEquals("waku", this.relay.protocol)
            assertEquals("testData", this.relay.data)
            assertEquals("587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303", this.symKey.keyAsHex)
            assertEquals("2", this.version)
        }
    }

    @Test
    fun `parse walletconnect uri to absolute string`() {
        val uri = EngineDO.WalletConnectUri(
            TopicVO("11112222244444"),
            SecretKey("0x12321321312312312321"),
            RelayProtocolOptionsVO("waku", "teeestData")
        )

        assertEquals(uri.toAbsoluteString(), "wc:11112222244444@2?relay-protocol=waku&relay-data=teeestData&symKey=0x12321321312312312321")

        val uri2 = EngineDO.WalletConnectUri(
            TopicVO("11112222244444"),
            SecretKey("0x12321321312312312321"),
            RelayProtocolOptionsVO("waku")
        )

        assertEquals(uri2.toAbsoluteString(), "wc:11112222244444@2?relay-protocol=waku&symKey=0x12321321312312312321")
    }

    @Test
    fun `extend session expiry less than 1 week`() {
        val currentExpiry: Long = 1646641841 //07.03
        val newExpiry: Long = 1646901496 //10.03
        Validator.validateSessionExtend(newExpiry, currentExpiry) {
            assertTrue(false)
        }
    }

    @Test
    fun `extend session expiry over 1 week`() {
        val currentExpiry: Long = 1646641841 //07.03
        val newExpiry: Long = 1647765496 //20.03

        Validator.validateSessionExtend(newExpiry, currentExpiry) {
            assertEquals(INVALID_EXTEND_TIME, it)
        }
    }

    @Test
    fun `extend session expiry less than current expiry`() {
        val currentExpiry: Long = 1646641841 //07.03
        val newExpiry: Long = 1646555896 //06.03

        Validator.validateSessionExtend(newExpiry, currentExpiry) {
            assertEquals(INVALID_EXTEND_TIME, it)
        }
    }

    @Test
    fun `test time periods in seconds`() {
        Time.fiveMinutesInSeconds.apply { assertEquals(this.compareTo(300), 0) }
        Time.dayInSeconds.apply { assertEquals(this.compareTo(86400), 0) }
        Time.weekInSeconds.apply { assertEquals(this.compareTo(604800), 0) }
        Time.monthInSeconds.apply { assertEquals(this.compareTo(2592000), 0) }
    }
}