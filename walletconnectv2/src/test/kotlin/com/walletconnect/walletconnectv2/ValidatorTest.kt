package com.walletconnect.walletconnectv2

import com.walletconnect.walletconnectv2.core.exceptions.EMPTY_CHAIN_LIST_MESSAGE
import com.walletconnect.walletconnectv2.core.exceptions.EMPTY_RPC_METHODS_LIST_MESSAGE
import com.walletconnect.walletconnectv2.core.exceptions.INVALID_NOTIFICATIONS_TYPES_MESSAGE
import com.walletconnect.walletconnectv2.engine.domain.Validator
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

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
        val blockchain = EngineDO.Blockchain(listOf("1"))
        val jsonRpc = EngineDO.JsonRpc(listOf("eth_sign"))
        var notifications: EngineDO.Notifications? = EngineDO.Notifications(listOf())
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
        var notifications: EngineDO.Notifications? = EngineDO.Notifications(listOf())

        val result1 = Validator.areNotificationTypesValid(notifications)
        assertEquals(result1, false)

        notifications = EngineDO.Notifications(listOf("", ""))
        val result2 = Validator.areNotificationTypesValid(notifications)
        assertEquals(result2, false)

        notifications = EngineDO.Notifications(listOf("1", "2"))
        val result3 = Validator.areNotificationTypesValid(notifications)
        assertEquals(result3, true)

        notifications = null
        val result4 = Validator.areNotificationTypesValid(notifications)
        assertEquals(result4, false)
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
}