package com.walletconnect.sign

import com.walletconnect.sign.ValidatorTest.Accounts.COSMOSHUB_4_1
import com.walletconnect.sign.ValidatorTest.Accounts.ETHEREUM_1
import com.walletconnect.sign.ValidatorTest.Accounts.ETHEREUM_2
import com.walletconnect.sign.ValidatorTest.Accounts.ETHEREUM_3
import com.walletconnect.sign.ValidatorTest.Accounts.ETHEREUM_4
import com.walletconnect.sign.ValidatorTest.Accounts.ETHEREUM_5
import com.walletconnect.sign.ValidatorTest.Accounts.KOVAN_1
import com.walletconnect.sign.ValidatorTest.Accounts.MATIC_1
import com.walletconnect.sign.ValidatorTest.Chains.COSMOSHUB_4
import com.walletconnect.sign.ValidatorTest.Chains.ETHEREUM
import com.walletconnect.sign.ValidatorTest.Chains.MATIC
import com.walletconnect.sign.ValidatorTest.Chains.OPTIMISM
import com.walletconnect.sign.ValidatorTest.Events.ACCOUNTS_CHANGED
import com.walletconnect.sign.ValidatorTest.Events.CHAIN_CHANGED
import com.walletconnect.sign.ValidatorTest.Events.COSMOS_EVENT
import com.walletconnect.sign.ValidatorTest.Events.SOME_EVENT
import com.walletconnect.sign.ValidatorTest.Methods.COSMOS_SIGNDIRECT
import com.walletconnect.sign.ValidatorTest.Methods.ETH_GETACCOUNTS
import com.walletconnect.sign.ValidatorTest.Methods.ETH_SIGN
import com.walletconnect.sign.ValidatorTest.Methods.PERSONAL_SIGN
import com.walletconnect.sign.ValidatorTest.Namespaces.COSMOS
import com.walletconnect.sign.ValidatorTest.Namespaces.EIP155
import com.walletconnect.sign.core.exceptions.*
import com.walletconnect.sign.core.model.vo.SecretKey
import com.walletconnect.sign.core.model.vo.TopicVO
import com.walletconnect.sign.core.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.sign.engine.domain.Validator
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toAbsoluteString
import com.walletconnect.sign.util.Time
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ValidatorTest {

    private object Namespaces {
        const val COSMOS = "cosmos"
        const val EIP155 = "eip155"
    }

    private object Chains {
        const val ETHEREUM = "eip155:1"
        const val OPTIMISM = "eip155:10"
        const val MATIC = "eip155:137"
        const val COSMOSHUB_4 = "cosmos:cosmoshub-4"
    }

    private object Accounts {
        const val ETHEREUM_1 = "eip155:1:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb"
        const val ETHEREUM_2 = "eip155:1:0x25caCa7f7Bf3A77b1738A8c98A666dd9e4C69A0C"
        const val ETHEREUM_3 = "eip155:1:0x2Fe1cC9b1DCe6E8e16C48bc6A7ABbAB3d10DA954"
        const val ETHEREUM_4 = "eip155:1:0xEA674fdDe714fd979de3EdF0F56AA9716B898ec8"
        const val ETHEREUM_5 = "eip155:1:0xEB2F31B0224222D774541BfF89A221e7eb15a17E"
        const val KOVAN_1 = "eip155:42:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb"
        const val MATIC_1 = "eip155:137:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb"
        const val COSMOSHUB_4_1 = "cosmos:cosmoshub-4:cosmos1t2uflqwqe0fsj0shcfkrvpukewcw40yjj6hdc0"
    }

    private object Methods {
        const val COSMOS_SIGNDIRECT = "cosmos_signDirect"
        const val ETH_SIGN = "eth_sign"
        const val ETH_GETACCOUNTS = "eth_getAccounts"
        const val PERSONAL_SIGN = "personalSign"
    }

    private object Events {
        const val COSMOS_EVENT = "someCosmosEvent"
        const val ACCOUNTS_CHANGED = "accountsChanged"
        const val CHAIN_CHANGED = "chainChanged"
        const val SOME_EVENT = "someEvent"
    }


    @Test
    fun `Proposal Namespaces MUST NOT have chains empty`() {
        val namespaces = mapOf(
            COSMOS to NamespaceVO.Proposal(
                chains = emptyList(), methods = listOf(COSMOS_SIGNDIRECT), events = listOf(COSMOS_EVENT), extensions = null
            )
        )
        var errorMessage: String? = null
        Validator.validateProposalNamespace(namespaces) { errorMessage = it.message }
        assertNotNull(errorMessage)
        assertEquals(NAMESPACE_CHAINS_MISSING_MESSAGE, errorMessage)
    }

    @Test
    fun `Proposal Namespaces chains MUST be CAIP-2 compliant`() {
        val namespaces = mapOf(
            EIP155 to NamespaceVO.Proposal(
                chains = listOf("42"), methods = listOf(ETH_SIGN), events = listOf(ACCOUNTS_CHANGED), extensions = null
            )
        )
        var errorMessage: String? = null
        Validator.validateProposalNamespace(namespaces) { errorMessage = it.message }
        assertNotNull(errorMessage)
        assertEquals(NAMESPACE_CHAINS_CAIP_2_MESSAGE, errorMessage)
    }

    @Test
    fun `Proposal Namespaces methods and events MAY be empty`() {
        val namespaces = mapOf(
            EIP155 to NamespaceVO.Proposal(
                chains = listOf(ETHEREUM), methods = emptyList(), events = emptyList(), extensions = null
            )
        )
        var errorMessage: String? = null
        Validator.validateProposalNamespace(namespaces) { errorMessage = it.message }
        assertNull(errorMessage)
    }

    @Test
    fun `All chains in the namespace MUST contain the namespace prefix`() {
        val namespaces = mapOf(
            EIP155 to NamespaceVO.Proposal(
                chains = listOf(ETHEREUM, COSMOSHUB_4), methods = listOf(PERSONAL_SIGN), events = listOf(CHAIN_CHANGED), extensions = null
            )
        )
        var errorMessage: String? = null
        Validator.validateProposalNamespace(namespaces) { errorMessage = it.message }
        assertNotNull(errorMessage)
        assertEquals(NAMESPACE_CHAINS_WRONG_NAMESPACE_MESSAGE, errorMessage)
    }

    @Test
    fun `Extension MUST NOT have chains empty`() {
        val namespaces = mapOf(
            EIP155 to NamespaceVO.Proposal(
                chains = listOf(ETHEREUM, MATIC), methods = listOf(PERSONAL_SIGN), events = emptyList(),
                extensions = listOf(
                    NamespaceVO.Proposal.Extension(
                        chains = emptyList(),
                        methods = listOf(ETH_GETACCOUNTS),
                        events = emptyList()
                    )
                )
            )
        )
        var errorMessage: String? = null
        Validator.validateProposalNamespace(namespaces) { errorMessage = it.message }
        assertNotNull(errorMessage)
        assertEquals(NAMESPACE_EXTENSION_CHAINS_MISSING_MESSAGE, errorMessage)
    }

    @Test
    fun `Namespace key must comply with CAIP-2 specification`() {
        val namespaces = mapOf(
            "" to NamespaceVO.Proposal(
                chains = listOf(":1"), methods = listOf(PERSONAL_SIGN), events = emptyList(), extensions = null
            ),
            "**" to NamespaceVO.Proposal(
                chains = listOf("**:1"), methods = listOf(PERSONAL_SIGN), events = emptyList(), extensions = null
            )
        )
        var errorMessage: String? = null
        Validator.validateProposalNamespace(namespaces) { errorMessage = it.message }
        assertNotNull(errorMessage)
        assertEquals(NAMESPACE_KEYS_CAIP_2_MESSAGE, errorMessage)
    }

    @Test
    fun `Session Namespaces MUST NOT have accounts empty`() {
        val proposalNamespaces = mapOf(
            COSMOS to NamespaceVO.Proposal(
                chains = emptyList(), methods = listOf(COSMOS_SIGNDIRECT), events = listOf(COSMOS_EVENT), extensions = null
            )
        )

        val sessionNamespaces = mapOf(
            COSMOS to NamespaceVO.Session(
                accounts = emptyList(), methods = listOf(COSMOS_SIGNDIRECT), events = listOf(COSMOS_EVENT), extensions = null
            )
        )
        var errorMessage: String? = null
        Validator.validateSessionNamespace(sessionNamespaces, proposalNamespaces) { errorMessage = it.message }
        assertNotNull(errorMessage)
        assertEquals(NAMESPACE_ACCOUNTS_MISSING_MESSAGE, errorMessage)
    }

    @Test
    fun `Session Namespaces addresses MUST be CAIP-10 compliant`() {
        val proposalNamespaces = mapOf(
            EIP155 to NamespaceVO.Proposal(
                chains = listOf(ETHEREUM), methods = listOf(ETH_SIGN), events = listOf(ACCOUNTS_CHANGED), extensions = null
            )
        )

        val namespaces = mapOf(
            EIP155 to NamespaceVO.Session(
                accounts = listOf("eip155:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb"),
                methods = listOf(ETH_SIGN),
                events = listOf(ACCOUNTS_CHANGED),
                extensions = null
            )
        )
        var errorMessage: String? = null
        Validator.validateSessionNamespace(namespaces, proposalNamespaces) { errorMessage = it.message }
        assertNotNull(errorMessage)
        assertEquals(NAMESPACE_ACCOUNTS_CAIP_10_MESSAGE, errorMessage)
    }

    @Test
    fun `Session Namespaces MUST approve all methods`() {
        val proposalNamespaces = mapOf(
            EIP155 to NamespaceVO.Proposal(
                chains = listOf(ETHEREUM), methods = listOf(ETH_SIGN), events = emptyList(), extensions = null
            )
        )

        val namespaces = mapOf(
            EIP155 to NamespaceVO.Session(
                accounts = listOf(ETHEREUM_1), methods = emptyList(), events = emptyList(), extensions = null
            )
        )
        var errorMessage: String? = null
        Validator.validateSessionNamespace(namespaces, proposalNamespaces) { errorMessage = it.message }
        assertNotNull(errorMessage)
        assertEquals(NAMESPACE_METHODS_MISSING_MESSAGE, errorMessage)
    }

    @Test
    fun `Session Namespaces MUST approve all events`() {
        val proposalNamespaces = mapOf(
            EIP155 to NamespaceVO.Proposal(
                chains = listOf(ETHEREUM), methods = emptyList(), events = listOf(CHAIN_CHANGED), extensions = null
            )
        )

        val namespaces = mapOf(
            EIP155 to NamespaceVO.Session(
                accounts = listOf(ETHEREUM_1), methods = emptyList(), events = emptyList(), extensions = null
            )
        )
        var errorMessage: String? = null
        Validator.validateSessionNamespace(namespaces, proposalNamespaces) { errorMessage = it.message }
        assertNotNull(errorMessage)
        assertEquals(NAMESPACE_EVENTS_MISSING_MESSAGE, errorMessage)
    }

    @Test
    fun `Session Namespaces MUST contain at least one account in requested chains`() {
        val proposalNamespaces = mapOf(
            EIP155 to NamespaceVO.Proposal(
                chains = listOf(ETHEREUM, OPTIMISM), methods = listOf(ETH_SIGN), events = listOf(ACCOUNTS_CHANGED), extensions = null
            )
        )

        val namespaces = mapOf(
            EIP155 to NamespaceVO.Session(
                accounts = listOf(ETHEREUM_1), methods = listOf(ETH_SIGN), events = listOf(ACCOUNTS_CHANGED), extensions = null
            )
        )
        var errorMessage: String? = null
        Validator.validateSessionNamespace(namespaces, proposalNamespaces) { errorMessage = it.message }
        assertNotNull(errorMessage)
        assertEquals(NAMESPACE_ACCOUNTS_MISSING_FOR_CHAINS_MESSAGE, errorMessage)
    }

    @Test
    fun `Session Namespaces MAY contain multiple accounts for one chain`() {
        val proposalNamespaces = mapOf(
            EIP155 to NamespaceVO.Proposal(
                chains = listOf(ETHEREUM), methods = listOf(ETH_SIGN), events = listOf(ACCOUNTS_CHANGED), extensions = null
            )
        )

        val namespaces = mapOf(
            EIP155 to NamespaceVO.Session(
                accounts = listOf(ETHEREUM_1, ETHEREUM_2, ETHEREUM_3, ETHEREUM_4, ETHEREUM_5),
                methods = listOf(ETH_SIGN),
                events = listOf(ACCOUNTS_CHANGED),
                extensions = null
            )
        )
        var errorMessage: String? = null
        Validator.validateSessionNamespace(namespaces, proposalNamespaces) { errorMessage = it.message }
        assertNull(errorMessage)
    }

    @Test
    fun `Session Namespaces MAY extend methods and events of Proposal Namespaces`() {
        val proposalNamespaces = mapOf(
            EIP155 to NamespaceVO.Proposal(
                chains = listOf(ETHEREUM), methods = listOf(ETH_SIGN), events = listOf(ACCOUNTS_CHANGED), extensions = null
            )
        )

        val namespaces = mapOf(
            EIP155 to NamespaceVO.Session(
                accounts = listOf(ETHEREUM_1),
                methods = listOf(ETH_SIGN, PERSONAL_SIGN),
                events = listOf(ACCOUNTS_CHANGED, SOME_EVENT),
                extensions = null
            )
        )
        var errorMessage: String? = null
        Validator.validateSessionNamespace(namespaces, proposalNamespaces) { errorMessage = it.message }
        assertNull(errorMessage)
    }

    @Test
    fun `All accounts in the namespace MUST contain the namespace prefix`() {
        val proposalNamespaces = mapOf(
            EIP155 to NamespaceVO.Proposal(
                chains = listOf(ETHEREUM), methods = listOf(ETH_SIGN), events = listOf(ACCOUNTS_CHANGED), extensions = null
            )
        )

        val namespaces = mapOf(
            EIP155 to NamespaceVO.Session(
                accounts = listOf(ETHEREUM_1, COSMOSHUB_4_1),
                methods = listOf(ETH_SIGN),
                events = listOf(ACCOUNTS_CHANGED),
                extensions = null
            )
        )
        var errorMessage: String? = null
        Validator.validateSessionNamespace(namespaces, proposalNamespaces) { errorMessage = it.message }
        assertNotNull(errorMessage)
        assertEquals(NAMESPACE_ACCOUNTS_WRONG_NAMESPACE_MESSAGE, errorMessage)
    }

    @Test
    fun `Session Namespaces MAY contain accounts from chains not defined in Proposal Namespaces`() {
        val proposalNamespaces = mapOf(
            EIP155 to NamespaceVO.Proposal(
                chains = listOf(ETHEREUM), methods = listOf(ETH_SIGN), events = listOf(ACCOUNTS_CHANGED), extensions = null
            )
        )

        val namespaces = mapOf(
            EIP155 to NamespaceVO.Session(
                accounts = listOf(ETHEREUM_1, KOVAN_1),
                methods = listOf(ETH_SIGN),
                events = listOf(ACCOUNTS_CHANGED),
                extensions = null
            )
        )
        var errorMessage: String? = null
        Validator.validateSessionNamespace(namespaces, proposalNamespaces) { errorMessage = it.message }
        assertNull(errorMessage)
    }

    @Test
    fun `Session Namespaces MUST have at least the same namespaces as the Proposal Namespaces`() {
        val proposalNamespaces = mapOf(
            EIP155 to NamespaceVO.Proposal(
                chains = listOf(MATIC, ETHEREUM), methods = listOf(ETH_SIGN), events = listOf(ACCOUNTS_CHANGED), extensions = null
            ),
            COSMOS to NamespaceVO.Proposal(
                chains = listOf(COSMOSHUB_4), methods = listOf(COSMOS_SIGNDIRECT), events = listOf(COSMOS_EVENT), extensions = null
            )
        )

        val namespaces = mapOf(
            EIP155 to NamespaceVO.Session(
                accounts = listOf(ETHEREUM_1, MATIC_1),
                methods = listOf(ETH_SIGN),
                events = listOf(ACCOUNTS_CHANGED),
                extensions = null
            )
        )
        var errorMessage: String? = null
        Validator.validateSessionNamespace(namespaces, proposalNamespaces) { errorMessage = it.message }
        assertNotNull(errorMessage)
        assertEquals(NAMESPACE_KEYS_MISSING_MESSAGE, errorMessage)
    }

    @Test
    fun `Extensions MAY be merged into namespace`() {
        val proposalNamespaces = mapOf(
            EIP155 to NamespaceVO.Proposal(
                chains = listOf(MATIC, ETHEREUM), methods = listOf(ETH_SIGN), events = listOf(ACCOUNTS_CHANGED),
                extensions = listOf(
                    NamespaceVO.Proposal.Extension(
                        chains = listOf(MATIC),
                        methods = listOf(PERSONAL_SIGN),
                        events = emptyList()
                    )
                )
            )
        )

        val namespaces = mapOf(
            EIP155 to NamespaceVO.Session(
                accounts = listOf(ETHEREUM_1, MATIC_1),
                methods = listOf(ETH_SIGN, PERSONAL_SIGN),
                events = listOf(ACCOUNTS_CHANGED),
                extensions = null
            )
        )
        var errorMessage: String? = null
        Validator.validateSessionNamespace(namespaces, proposalNamespaces) { errorMessage = it.message }
        assertNull(errorMessage)
    }

    @Test
    fun `Session Namespaces extensions MAY extend methods and events of Proposal Namespaces extensions`() {
        val proposalNamespaces = mapOf(
            EIP155 to NamespaceVO.Proposal(
                chains = listOf(MATIC, ETHEREUM), methods = emptyList(), events = listOf(CHAIN_CHANGED),
                extensions = listOf(
                    NamespaceVO.Proposal.Extension(
                        chains = listOf(MATIC),
                        methods = listOf(ETH_SIGN),
                        events = emptyList()
                    )
                )
            )
        )

        val namespaces = mapOf(
            EIP155 to NamespaceVO.Session(
                accounts = listOf(ETHEREUM_1, MATIC_1),
                methods = emptyList(),
                events = listOf(CHAIN_CHANGED),
                extensions = listOf(
                    NamespaceVO.Session.Extension(
                        accounts = listOf(MATIC_1), methods = listOf(ETH_SIGN, PERSONAL_SIGN), events = listOf(ACCOUNTS_CHANGED)
                    )
                )
            )
        )
        var errorMessage: String? = null
        Validator.validateSessionNamespace(namespaces, proposalNamespaces) { errorMessage = it.message }
        assertNull(errorMessage)
    }

    @Test
    fun `Session Namespaces extensions MAY contain accounts from chains not defined in Proposal Namespaces extensions`() {
        val proposalNamespaces = mapOf(
            EIP155 to NamespaceVO.Proposal(
                chains = listOf(MATIC, ETHEREUM), methods = listOf(ETH_SIGN), events = listOf(ACCOUNTS_CHANGED),
                extensions = listOf(
                    NamespaceVO.Proposal.Extension(
                        chains = listOf(MATIC), methods = listOf(PERSONAL_SIGN), events = listOf(CHAIN_CHANGED)
                    )
                )
            )
        )

        val namespaces = mapOf(
            EIP155 to NamespaceVO.Session(
                accounts = listOf(ETHEREUM_1, MATIC_1),
                methods = listOf(ETH_SIGN),
                events = listOf(ACCOUNTS_CHANGED),
                extensions = listOf(
                    NamespaceVO.Session.Extension(
                        accounts = listOf(MATIC_1, KOVAN_1), methods = listOf(PERSONAL_SIGN), events = listOf(CHAIN_CHANGED)
                    )
                )
            )
        )
        var errorMessage: String? = null
        Validator.validateSessionNamespace(namespaces, proposalNamespaces) { errorMessage = it.message }
        assertNull(errorMessage)
    }

    @Test
    fun `Session Namespaces MAY add extensions not defined in Proposal Namespaces extensions`() {
        val proposalNamespaces = mapOf(
            EIP155 to NamespaceVO.Proposal(
                chains = listOf(MATIC, ETHEREUM), methods = listOf(ETH_SIGN), events = listOf(ACCOUNTS_CHANGED), extensions = null
            )
        )

        val namespaces = mapOf(
            EIP155 to NamespaceVO.Session(
                accounts = listOf(ETHEREUM_1, MATIC_1),
                methods = listOf(ETH_SIGN),
                events = listOf(ACCOUNTS_CHANGED),
                extensions = listOf(
                    NamespaceVO.Session.Extension(
                        accounts = listOf(MATIC_1, KOVAN_1), methods = listOf(PERSONAL_SIGN), events = listOf(CHAIN_CHANGED)
                    )
                )
            )
        )
        var errorMessage: String? = null
        Validator.validateSessionNamespace(namespaces, proposalNamespaces) { errorMessage = it.message }
        assertNull(errorMessage)
    }

    @Test
    fun `validate chain id against CAIP2 standard`() {
        Validator.isChainIdCAIP2Compliant("").apply { assertEquals(this, false) }
        Validator.isChainIdCAIP2Compliant("bip122:000000000019d6689c085ae165831e93").apply { assertEquals(this, true) }
        Validator.isChainIdCAIP2Compliant("cosmos:cosmoshub-2").apply { assertEquals(this, true) }
        Validator.isChainIdCAIP2Compliant("chainstd:23-33").apply { assertEquals(this, true) }
        Validator.isChainIdCAIP2Compliant("chainasdasdasdasdasdasdasdsastd:23-33").apply { assertEquals(this, false) }
        Validator.isChainIdCAIP2Compliant("cosmoscosmoshub-2").apply { assertEquals(this, false) }
        Validator.isChainIdCAIP2Compliant(":cosmoshub-2").apply { assertEquals(this, false) }
        Validator.isChainIdCAIP2Compliant("cosmos:").apply { assertEquals(this, false) }
        Validator.isChainIdCAIP2Compliant(":").apply { assertEquals(this, false) }
        Validator.isChainIdCAIP2Compliant("123:123").apply { assertEquals(this, true) }
    }

    @Test
    fun `is chain id valid test`() {
        Validator.isAccountIdCAIP10Compliant("").apply { assertEquals(this, false) }
        Validator.isAccountIdCAIP10Compliant("1231:dadd").apply { assertEquals(this, false) }
        Validator.isAccountIdCAIP10Compliant("0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb@eip155:1").apply { assertEquals(this, false) }
        Validator.isAccountIdCAIP10Compliant("polkadot:b0a8d493285c2df73290dfb7e61f870f:5hmuyxw9xdgbpptgypokw4thfyoe3ryenebr381z9iaegmfy")
            .apply { assertEquals(this, true) }
        Validator.isAccountIdCAIP10Compliant("polkadot:b0a8d493285c2df73290dfb7e61f870f:5hmuy:xw9xdgbpptgypokw4thfyoe3ryenebr381z9iaegmfy")
            .apply { assertEquals(this, false) }
        Validator.isAccountIdCAIP10Compliant("polkadotb0a8d493285c2df73290dfb7e61f870f:b0a8d493285c2df73290dfb7e61f870f:5hmuy:xw9xdgbpptgypokw4thfyoe3ryenebr381z9iaegmfy")
            .apply { assertEquals(this, false) }
        Validator.isAccountIdCAIP10Compliant("::").apply { assertEquals(this, false) }
        Validator.isAccountIdCAIP10Compliant("a:s:d").apply { assertEquals(this, false) }
        Validator.isAccountIdCAIP10Compliant("a:s").apply { assertEquals(this, false) }
        Validator.isAccountIdCAIP10Compliant("chainstd:8c3444cf8970a9e41a706fab93e337a6c4:6d9b0b4b9994e8a6afbd3dc3ed983cd51c755afb27cd1dc7825ef59c134a39f7")
            .apply { assertEquals(this, false) }
    }

    @Test
    fun `Authorizing not approved event`() {
        val namespaces = mapOf(
            EIP155 to NamespaceVO.Session(
                accounts = listOf(ETHEREUM_1),
                methods = listOf(ETH_SIGN),
                events = listOf(CHAIN_CHANGED),
                extensions = null
            )
        )
        var errorMessage: String? = null
        Validator.validateChainIdWithEventAuthorisation(ETHEREUM, ACCOUNTS_CHANGED, namespaces) { errorMessage = it.message }
        assertNotNull(errorMessage)
        assertEquals(UNAUTHORIZED_EVENT_MESSAGE, errorMessage)
    }

    @Test
    fun `Authorizing not approved method`() {
        val namespaces = mapOf(
            EIP155 to NamespaceVO.Session(
                accounts = listOf(ETHEREUM_1),
                methods = listOf(ETH_SIGN),
                events = listOf(CHAIN_CHANGED),
                extensions = null
            )
        )
        var errorMessage: String? = null
        Validator.validateChainIdWithMethodAuthorisation(ETHEREUM, PERSONAL_SIGN, namespaces) { errorMessage = it.message }
        assertNotNull(errorMessage)
        assertEquals(UNAUTHORIZED_METHOD_MESSAGE, errorMessage)
    }

    @Test
    fun `Authorizing somewhere approved event but not by requested chain`() {
        val namespaces = mapOf(
            EIP155 to NamespaceVO.Session(
                accounts = listOf(ETHEREUM_1),
                methods = listOf(ETH_SIGN),
                events = listOf(CHAIN_CHANGED),
                extensions = null
            )
        )
        var errorMessage: String? = null
        Validator.validateChainIdWithEventAuthorisation(OPTIMISM, CHAIN_CHANGED, namespaces) { errorMessage = it.message }
        assertNotNull(errorMessage)
        assertEquals(UNAUTHORIZED_EVENT_MESSAGE, errorMessage)
    }

    @Test
    fun `Authorizing somewhere approved method but not by requested chain`() {
        val namespaces = mapOf(
            EIP155 to NamespaceVO.Session(
                accounts = listOf(ETHEREUM_1),
                methods = listOf(ETH_SIGN),
                events = listOf(CHAIN_CHANGED),
                extensions = null
            )
        )
        var errorMessage: String? = null
        Validator.validateChainIdWithMethodAuthorisation(OPTIMISM, ETH_SIGN, namespaces) { errorMessage = it.message }
        assertNotNull(errorMessage)
        assertEquals(UNAUTHORIZED_METHOD_MESSAGE, errorMessage)
    }

    @Test
    fun `Authorizing approved event`() {
        val namespaces = mapOf(
            EIP155 to NamespaceVO.Session(
                accounts = listOf(ETHEREUM_1),
                methods = listOf(ETH_SIGN),
                events = listOf(CHAIN_CHANGED),
                extensions = null
            )
        )
        var errorMessage: String? = null
        Validator.validateChainIdWithEventAuthorisation(ETHEREUM, CHAIN_CHANGED, namespaces) { errorMessage = it.message }
        assertNull(errorMessage)
    }

    @Test
    fun `Authorizing approved method`() {
        val namespaces = mapOf(
            EIP155 to NamespaceVO.Session(
                accounts = listOf(ETHEREUM_1),
                methods = listOf(ETH_SIGN),
                events = listOf(CHAIN_CHANGED),
                extensions = null
            )
        )
        var errorMessage: String? = null
        Validator.validateChainIdWithMethodAuthorisation(ETHEREUM, ETH_SIGN, namespaces) { errorMessage = it.message }
        assertNull(errorMessage)
    }

    @Test
    fun `is event valid test`() {
        var event = EngineDO.Event("", "data", ETHEREUM)
        var errorMessage: String? = null
        Validator.validateEvent(event) {
            errorMessage = it.message
        }
        assertEquals(INVALID_EVENT_MESSAGE, errorMessage)


        event = EngineDO.Event("someName", "", ETHEREUM)
        errorMessage = null
        Validator.validateEvent(event) {
            errorMessage = it.message
        }
        assertEquals(INVALID_EVENT_MESSAGE, errorMessage)

        event = EngineDO.Event("someName", "someData", "")
        errorMessage = null
        Validator.validateEvent(event) {
            errorMessage = it.message
        }
        assertEquals(INVALID_EVENT_MESSAGE, errorMessage)

        event = EngineDO.Event("someName", "someData", "1")
        errorMessage = null
        Validator.validateEvent(event) {
            errorMessage = it.message
        }
        assertEquals(INVALID_EVENT_MESSAGE, errorMessage)
    }

    @Test
    fun `is request valid test`() {
        var request = EngineDO.Request("", "someMethod", "someParams", ETHEREUM)
        var errorMessage: String? = null
        Validator.validateSessionRequest(request) {
            errorMessage = it.message
        }
        assertEquals(INVALID_REQUEST_MESSAGE, errorMessage)

        request = EngineDO.Request("someTopic", "", "someParams", ETHEREUM)
        errorMessage = null
        Validator.validateSessionRequest(request) {
            errorMessage = it.message
        }
        assertEquals(INVALID_REQUEST_MESSAGE, errorMessage)

        request = EngineDO.Request("someTopic", "someMethod", "", ETHEREUM)
        errorMessage = null
        Validator.validateSessionRequest(request) {
            errorMessage = it.message
        }
        assertEquals(INVALID_REQUEST_MESSAGE, errorMessage)

        request = EngineDO.Request("someTopic", "someMethod", "someParams", "")
        errorMessage = null
        Validator.validateSessionRequest(request) {
            errorMessage = it.message
        }
        assertEquals(INVALID_REQUEST_MESSAGE, errorMessage)

        request = EngineDO.Request("someTopic", "someMethod", "someParams", "1")
        errorMessage = null
        Validator.validateSessionRequest(request) {
            errorMessage = it.message
        }
        assertEquals(INVALID_REQUEST_MESSAGE, errorMessage)
    }

    @Test
    fun `validate WC uri test`() {
        val validUri =
            "wc:7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9@2?relay-protocol=iridium&symKey=587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303"

        Validator.validateWCUri("").apply { assertEquals(null, this) }
        Validator.validateWCUri(validUri).apply {
            assertNotNull(this)
            assertEquals("7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9", this.topic.value)
            assertEquals("iridium", this.relay.protocol)
            assertEquals("587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303", this.symKey.keyAsHex)
            assertEquals("2", this.version)
        }

        val noTopicInvalidUri =
            "wc:@2?relay-protocol=iridium&symKey=587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303"
        Validator.validateWCUri(noTopicInvalidUri).apply { assertNull(this) }

        val noPrefixInvalidUri =
            "7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9@2?relay-protocol=iridium&symKey=587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303"
        Validator.validateWCUri(noPrefixInvalidUri).apply { assertNull(this) }

        val noSymKeyInvalidUri =
            "wc:7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9@2?relay-protocol=iridium&symKey="
        Validator.validateWCUri(noSymKeyInvalidUri).apply { assertNull(this) }

        val noProtocolTypeInvalidUri =
            "wc:7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9@2?relay-protocol=&symKey=587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303"
        Validator.validateWCUri(noProtocolTypeInvalidUri).apply { assertNull(this) }
    }

    @Test
    fun `validate WC uri test optional data field`() {
        val validUri =
            "wc:7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9@2?relay-protocol=iridium&relay-data=testData&symKey=587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303"

        Validator.validateWCUri("").apply { assertEquals(null, this) }
        Validator.validateWCUri(validUri).apply {
            assertNotNull(this)
            assertEquals("7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9", this.topic.value)
            assertEquals("iridium", this.relay.protocol)
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
            RelayProtocolOptionsVO("iridium", "teeestData")
        )

        assertEquals(uri.toAbsoluteString(), "wc:11112222244444@2?relay-protocol=iridium&relay-data=teeestData&symKey=0x12321321312312312321")

        val uri2 = EngineDO.WalletConnectUri(
            TopicVO("11112222244444"),
            SecretKey("0x12321321312312312321"),
            RelayProtocolOptionsVO("iridium")
        )

        assertEquals(uri2.toAbsoluteString(), "wc:11112222244444@2?relay-protocol=iridium&symKey=0x12321321312312312321")
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
            assertEquals(INVALID_EXTEND_TIME, it.message)
        }
    }

    @Test
    fun `extend session expiry less than current expiry`() {
        val currentExpiry: Long = 1646641841 //07.03
        val newExpiry: Long = 1646555896 //06.03

        Validator.validateSessionExtend(newExpiry, currentExpiry) {
            assertEquals(INVALID_EXTEND_TIME, it.message)
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