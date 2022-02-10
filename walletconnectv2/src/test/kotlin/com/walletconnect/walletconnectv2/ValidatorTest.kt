package com.walletconnect.walletconnectv2

import com.walletconnect.walletconnectv2.core.exceptions.client.*
import com.walletconnect.walletconnectv2.engine.domain.Validator
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ValidatorTest {

    @Test
    fun `check correct error message when blockchain permissions are invalid `() {
        val blockchain = EngineDO.Blockchain(listOf())
        val jsonRpc = EngineDO.JsonRpc(listOf())
        val permissions = EngineDO.SessionPermissions(blockchain, jsonRpc)

        Validator.validateSessionPermissions(permissions) { errorMessage ->
            assertEquals(EMPTY_CHAIN_LIST_MESSAGE, errorMessage)
        }
    }

    @Test
    fun `check correct error message when json rpc permissions are invalid `() {
        val blockchain = EngineDO.Blockchain(listOf("1"))
        val jsonRpc = EngineDO.JsonRpc(listOf())
        val permissions = EngineDO.SessionPermissions(blockchain, jsonRpc)

        Validator.validateSessionPermissions(permissions) { errorMessage ->
            assertEquals(EMPTY_RPC_METHODS_LIST_MESSAGE, errorMessage)
        }
    }

    @Test
    fun `check correct error message when notifications permissions are invalid `() {
        val blockchain = EngineDO.Blockchain(listOf("1:aa"))
        val jsonRpc = EngineDO.JsonRpc(listOf("eth_sign"))
        var notifications: EngineDO.Notifications? = EngineDO.Notifications(listOf())
        val permissions = EngineDO.SessionPermissions(blockchain, jsonRpc, notifications)

        Validator.validateSessionPermissions(permissions) { errorMessage ->
            assertEquals(INVALID_NOTIFICATIONS_TYPES_MESSAGE, errorMessage)
        }
    }

    @Test
    fun `check correct error message when notifications permissions are null `() {
        val blockchain = EngineDO.Blockchain(listOf("cosmos:cosmoshub-2"))
        val jsonRpc = EngineDO.JsonRpc(listOf("eth_sign"))

        var notifications: EngineDO.Notifications? = null
        val permissions = EngineDO.SessionPermissions(blockchain, jsonRpc, notifications)

        Validator.validateSessionPermissions(permissions) { errorMessage ->
            assertEquals(INVALID_NOTIFICATIONS_TYPES_MESSAGE, errorMessage)
        }
    }

    @Test
    fun `are json rpc permissions valid`() {
        var jsonRpc = EngineDO.JsonRpc(listOf())

        val result1 = Validator.isJsonRpcValid(jsonRpc)
        assertEquals(result1, false)

        jsonRpc = EngineDO.JsonRpc(listOf("", ""))
        val result2 = Validator.isJsonRpcValid(jsonRpc)
        assertEquals(result2, false)

        jsonRpc = EngineDO.JsonRpc(listOf("personal_sign", "eth_sign"))
        val result3 = Validator.isJsonRpcValid(jsonRpc)
        assertEquals(result3, true)
    }

    @Test
    fun `are blockchain permissions not empty`() {
        var blockchain = EngineDO.Blockchain(listOf())

        val result1 = Validator.isBlockchainValid(blockchain)
        assertEquals(result1, false)

        blockchain = EngineDO.Blockchain(listOf("", ""))
        val result2 = Validator.isBlockchainValid(blockchain)
        assertEquals(result2, false)

        blockchain = EngineDO.Blockchain(listOf("1", "2"))
        val result3 = Validator.isBlockchainValid(blockchain)
        assertEquals(result3, true)
    }

    @Test
    fun `are notifications permissions valid`() {
        var notifications: EngineDO.Notifications = EngineDO.Notifications(listOf())

        val result1 = Validator.areNotificationTypesValid(notifications)
        assertEquals(result1, false)

        notifications = EngineDO.Notifications(listOf("", ""))
        val result2 = Validator.areNotificationTypesValid(notifications)
        assertEquals(result2, false)

        notifications = EngineDO.Notifications(listOf("1", "2"))
        val result3 = Validator.areNotificationTypesValid(notifications)
        assertEquals(result3, true)
    }

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
    fun `check correct error message when accounts are empty`() {
        val accounts = listOf("")
        val chains = listOf("as", "bc")
        Validator.validateAccounts(accounts, chains) { errorMessage ->
            assertEquals(errorMessage, EMPTY_ACCOUNT_LIST_MESSAGE)
        }
    }

    @Test
    fun `check correct error message when accounts are invalid`() {
        val accounts = listOf("as11", "1234")
        val chains = listOf("")
        Validator.validateAccounts(accounts, chains) { errorMessage ->
            assertEquals(errorMessage, WRONG_ACCOUNT_ID_FORMAT_MESSAGE)
        }
    }

    @Test
    fun `check correct error message when accounts are unauthorized`() {
        val accounts = listOf(
            "bip122:000000000019d6689c085ae165831e93:128Lkh3S7CkDTBZ8W7BbpsN3YYizJMp8p6",
            "polkadot:b0a8d493285c2df73290dfb7e61f870f:5hmuyxw9xdgbpptgypokw4thfyoe3ryenebr381z9iaegmfy"
        )
        val chains = listOf("")
        Validator.validateAccounts(accounts, chains) { errorMessage ->
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
        Validator.areChainIdsIncludedInPermissions(emptyList(), emptyList()).apply { assertEquals(this, false) }
        Validator.areChainIdsIncludedInPermissions(listOf(""), listOf("")).apply { assertEquals(this, false) }
        Validator.areChainIdsIncludedInPermissions(listOf("as"), listOf("")).apply { assertEquals(this, false) }
        Validator.areChainIdsIncludedInPermissions(listOf("as"), listOf("ss")).apply { assertEquals(this, false) }
        Validator.areChainIdsIncludedInPermissions(
            listOf(
                "bip122:000000000019d6689c085ae165831e93:128Lkh3S7CkDTBZ8W7BbpsN3YYizJMp8p6",
                "polkadot:b0a8d493285c2df73290dfb7e61f870f:5hmuyxw9xdgbpptgypokw4thfyoe3ryenebr381z9iaegmfy"
            ), listOf("ss", "aa")
        ).apply { assertEquals(this, false) }

        Validator.areChainIdsIncludedInPermissions(
            listOf(
                "bip122:128Lkh3S7CkDTBZ8W7BbpsN3YYizJMp8p6",
                "polkadot:5hmuyxw9xdgbpptgypokw4thfyoe3ryenebr381z9iaegmfy"
            ), listOf("bip122:000000000019d6689c085ae165831e93", "polkadot:b0a8d493285c2df73290dfb7e61f870f")
        ).apply { assertEquals(this, false) }

        Validator.areChainIdsIncludedInPermissions(
            listOf(
                "bip122:000000000019d6689c085ae165831e93:128Lkh3S7CkDTBZ8W7BbpsN3YYizJMp8p6",
                "polkadot:b0a8d493285c2df73290dfb7e61f870f:5hmuyxw9xdgbpptgypokw4thfyoe3ryenebr381z9iaegmfy"
            ), listOf("bip122:000000000019d6689c085ae165831e93", "polkadot:b0a8d493285c2df73290dfb7e61f870f")
        ).apply { assertEquals(this, true) }

        Validator.areChainIdsIncludedInPermissions(
            listOf(
                "bip122:000000000019d6689c085ae165831e93:128Lkh3S7CkDTBZ8W7BbpsN3YYizJMp8p6",
                "polkadot:b0a8d493285c2df73290dfb7e61f870f:5hmuyxw9xdgbpptgypokw4thfyoe3ryenebr381z9iaegmfy"
            ), listOf("polkadot:b0a8d493285c2df73290dfb7e61f870f")
        ).apply { assertEquals(this, false) }

        Validator.areChainIdsIncludedInPermissions(
            listOf(
                "bip122:000000000019d6689c085ae165831e93:128Lkh3S7CkDTBZ8W7BbpsN3YYizJMp8p6"
            ), listOf("polkadot:b0a8d493285c2df73290dfb7e61f870f")
        ).apply { assertEquals(this, false) }

        Validator.areChainIdsIncludedInPermissions(
            listOf(
                "bip122:000000000019d6689c085ae165831e93:128Lkh3S7CkDTBZ8W7BbpsN3YYizJMp8p6"
            ), listOf("bip122:000000000019d6689c085ae165831e93")
        ).apply { assertEquals(this, true) }
    }

    @Test
    fun `is chain id authorize test`() {
        Validator.validateChainIdAuthorization(
            "bip122:000000000019d6689c085ae165831e93",
            listOf()
        ) { assertEquals(UNAUTHORIZED_CHAIN_ID_MESSAGE, it) }

        Validator.validateChainIdAuthorization(
            "",
            listOf("polkadot:b0a8d493285c2df73290dfb7e61f870f:")
        ) {
            assertEquals(UNAUTHORIZED_CHAIN_ID_MESSAGE, it)
        }

        Validator.validateChainIdAuthorization(
            "bip122:000000000019d6689c085ae165831e93",
            listOf("polkadot:b0a8d493285c2df73290dfb7e61f870f, bip122:000000000019d6689c085ae165831e93")
        ) {
            assertEquals(UNAUTHORIZED_CHAIN_ID_MESSAGE, it)
        }
    }

    @Test
    fun `is notification valid test`() {
        var notification = EngineDO.Notification("", "data")
        Validator.validateNotification(notification) {
            assertEquals(INVALID_NOTIFICATION_MESSAGE, it)
        }

        notification = EngineDO.Notification("type", "")
        Validator.validateNotification(notification) {
            assertEquals(INVALID_NOTIFICATION_MESSAGE, it)
        }

        notification = EngineDO.Notification("", "")
        Validator.validateNotification(notification) {
            assertEquals(INVALID_NOTIFICATION_MESSAGE, it)
        }
    }

    @Test
    fun `is session proposal valid test`() {
        val proposal = EngineDO.SessionProposal(
            "name", "dsc", "", listOf(), listOf(),
            listOf(), listOf(), "", "", false, 1L, listOf(), ""
        )
        Validator.validateProposalFields(proposal) { assertEquals(INVALID_SESSION_PROPOSAL_MESSAGE, it) }
    }

    @Test
    fun `validate WC uri test`() {
        val validUri =
            "wc:0ec08854dea4a7cc8ede647c163e8f5dafd39c371f0011b30907c98d289daf33@2?controller=false&publicKey=4699519eaebe8e0d171cd5ff349552704b078b2ae66999fc1addd8710bfa1249&relay=%7B%22protocol%22%3A%22waku%22%7D"

        Validator.validateWCUri("").apply { assertEquals(null, this) }
        Validator.validateWCUri(validUri).apply {
            assertNotNull(this)
            assertEquals("0ec08854dea4a7cc8ede647c163e8f5dafd39c371f0011b30907c98d289daf33", this.topic.value)
            assertEquals("waku", this.relay.protocol)
            assertEquals("4699519eaebe8e0d171cd5ff349552704b078b2ae66999fc1addd8710bfa1249", this.publicKey.keyAsHex)
            assertEquals(false, this.isController)
            assertEquals("2", this.version)
        }

        val noTopicInvalidUri =
            "wc:@2?controller=false&publicKey=4699519eaebe8e0d171cd5ff349552704b078b2ae66999fc1addd8710bfa1249&relay=%7B%22protocol%22%3A%22waku%22%7D"
        Validator.validateWCUri(noTopicInvalidUri).apply { assertNull(this) }

        val noControllerFlagInvalidUri =
            "wc:0ec08854dea4a7cc8ede647c163e8f5dafd39c371f0011b30907c98d289daf33@2?controller=&publicKey=4699519eaebe8e0d171cd5ff349552704b078b2ae66999fc1addd8710bfa1249&relay=%7B%22protocol%22%3A%22waku%22%7D"
        Validator.validateWCUri(noControllerFlagInvalidUri).apply { assertNull(this) }

        val noPrefixInvalidUri =
            "0ec08854dea4a7cc8ede647c163e8f5dafd39c371f0011b30907c98d289daf33@2?controller=false&publicKey=4699519eaebe8e0d171cd5ff349552704b078b2ae66999fc1addd8710bfa1249&relay=%7B%22protocol%22%3A%22waku%22%7D"
        Validator.validateWCUri(noPrefixInvalidUri).apply { assertNull(this) }

        val noPubKeyInvalidUri =
            "wc:0ec08854dea4a7cc8ede647c163e8f5dafd39c371f0011b30907c98d289daf33@2?controller=false&relay=%7B%22protocol%22%3A%22waku%22%7D"
        Validator.validateWCUri(noPubKeyInvalidUri).apply { assertNull(this) }

        val noProtocolTypeInvalidUri =
            "wc:0ec08854dea4a7cc8ede647c163e8f5dafd39c371f0011b30907c98d289daf33@2?controller=false&publicKey=4699519eaebe8e0d171cd5ff349552704b078b2ae66999fc1addd8710bfa1249&relay=%7B%22protocol%22%3A%%22%7D"
        Validator.validateWCUri(noProtocolTypeInvalidUri).apply { assertNull(this) }
    }
}