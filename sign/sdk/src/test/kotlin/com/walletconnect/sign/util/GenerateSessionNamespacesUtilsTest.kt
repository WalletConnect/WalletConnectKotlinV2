package com.walletconnect.sign.util

import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.utils.generateApprovedNamespaces
import com.walletconnect.sign.client.utils.normalizeNamespaces
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith

class GenerateSessionNamespacesUtilsTest {

    @Test
    fun testNamespacesNormalizationFromChainIndexing() {
        val namespaces = mapOf(
            "eip155:1" to NamespaceVO.Proposal(methods = listOf("method_1", "method_2"), events = listOf("event_1", "event_2")),
            "eip155:2" to NamespaceVO.Proposal(methods = listOf("method_11", "method_22"), events = listOf("event_11", "event_22")),
        )

        val normalizedNamespaces = mapOf(
            "eip155" to NamespaceVO.Proposal(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf("method_1", "method_2", "method_11", "method_22"),
                events = listOf("event_1", "event_2", "event_11", "event_22")
            )
        )

        val result = normalizeNamespaces(namespaces)
        assertEquals(normalizedNamespaces, result)
    }

    @Test
    fun testNamespacesNormalizationMixedApproach() {
        val namespaces = mapOf(
            "eip155:1" to NamespaceVO.Proposal(methods = listOf("method_1", "method_2"), events = listOf("event_1", "event_2")),
            "eip155" to NamespaceVO.Proposal(chains = listOf("eip155:2"), methods = listOf("method_11", "method_22"), events = listOf("event_11", "event_22")),
        )

        val normalizedNamespaces = mapOf(
            "eip155" to NamespaceVO.Proposal(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf("method_1", "method_2", "method_11", "method_22"),
                events = listOf("event_1", "event_2", "event_11", "event_22")
            )
        )

        val result = normalizeNamespaces(namespaces)
        assertEquals(normalizedNamespaces, result)
    }

    @Test
    fun testNamespacesNormalizationWithNormalizedMap() {
        val namespaces = mapOf(
            "eip155" to NamespaceVO.Proposal(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf("method_1", "method_2", "method_11", "method_22"),
                events = listOf("event_1", "event_2", "event_11", "event_22")
            ),
        )

        val normalizedNamespaces = mapOf(
            "eip155" to NamespaceVO.Proposal(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf("method_1", "method_2", "method_11", "method_22"),
                events = listOf("event_1", "event_2", "event_11", "event_22")
            )
        )

        val result = normalizeNamespaces(namespaces)
        assertEquals(normalizedNamespaces, result)
    }

    /* All test cases and configs can be found: https://docs.google.com/spreadsheets/d/1uc7lLWvx7tjgq_iQYylHVLNcs4F5z7jsnq_2f7ouGM8/edit#gid=0 */

    @Test
    fun `generate approved namespaces - config 1 - optional method`() {
        val required = mapOf("eip155" to Sign.Model.Namespace.Proposal(chains = listOf("eip155:1"), methods = listOf("personal_sign"), events = listOf("chainChanged")))
        val optional = mapOf("eip155" to Sign.Model.Namespace.Proposal(chains = listOf("eip155:1"), methods = listOf("eth_sendTransaction"), events = listOf("")))
        val supported = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:137", "eip155:3"),
                methods = listOf("personal_sign", "eth_sendTransaction", "eth_signTransaction"),
                events = listOf("chainChanged"),
                accounts = listOf("eip155:1:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092", "eip155:137:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092", "eip155:3:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092")
            )
        )
        val proposal = Sign.Model.SessionProposal("", "", "", "", listOf(), requiredNamespaces = required, optionalNamespaces = optional, mapOf(), "", "", "")

        val approved = generateApprovedNamespaces(proposal, supported)
        val expected = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1"),
                methods = listOf("personal_sign", "eth_sendTransaction"),
                events = listOf("chainChanged"),
                accounts = listOf("eip155:1:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092")
            )
        )

        assertEquals(expected, approved)
    }

    @Test
    fun `generate approved namespaces - config 2 - optional chain`() {
        val required = mapOf("eip155" to Sign.Model.Namespace.Proposal(chains = listOf("eip155:1"), methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged")))
        val optional = mapOf("eip155" to Sign.Model.Namespace.Proposal(chains = listOf("eip155:2"), methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged")))
        val supported = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:2", "eip155:3"),
                methods = listOf("personal_sign", "eth_sendTransaction", "eth_signTransaction"),
                events = listOf("chainChanged"),
                accounts = listOf("eip155:1:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092", "eip155:2:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092")
            )
        )
        val proposal = Sign.Model.SessionProposal("", "", "", "", listOf(), requiredNamespaces = required, optionalNamespaces = optional, mapOf(), "", "", "")

        val approved = generateApprovedNamespaces(proposal, supported)
        val expected = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf("personal_sign", "eth_sendTransaction"),
                events = listOf("chainChanged"),
                accounts = listOf("eip155:1:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092", "eip155:2:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092")
            )
        )

        assertEquals(expected, approved)
    }

    @Test
    fun `generate approved namespaces - config 3 - inline chain`() {
        val required = mapOf("eip155:1" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged")))
        val optional = mapOf("eip155:2" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged")))
        val supported = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:2", "eip155:3"),
                methods = listOf("personal_sign", "eth_sendTransaction", "eth_signTransaction"),
                events = listOf("chainChanged"),
                accounts = listOf("eip155:1:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092", "eip155:2:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092")
            )
        )
        val proposal = Sign.Model.SessionProposal("", "", "", "", listOf(), requiredNamespaces = required, optionalNamespaces = optional, mapOf(), "", "", "")

        val approved = generateApprovedNamespaces(proposal, supported)
        val expected = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf("personal_sign", "eth_sendTransaction"),
                events = listOf("chainChanged"),
                accounts = listOf("eip155:1:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092", "eip155:2:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092")
            )
        )

        assertEquals(expected, approved)
    }

    @Test
    fun `generate approved namespaces - config 4 - multiple inline chains`() {
        val required = mapOf(
            "eip155:1" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged")),
            "eip155:2" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged"))
        )
        val optional = mapOf("eip155" to Sign.Model.Namespace.Proposal(chains = listOf("eip155:3"), methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged")))
        val supported = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:2", "eip155:3"),
                methods = listOf("personal_sign", "eth_sendTransaction", "eth_signTransaction"),
                events = listOf("chainChanged"),
                accounts = listOf("eip155:1:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092", "eip155:2:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092", "eip155:3:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092")
            )
        )
        val proposal = Sign.Model.SessionProposal("", "", "", "", listOf(), requiredNamespaces = required, optionalNamespaces = optional, mapOf(), "", "", "")

        val approved = generateApprovedNamespaces(proposal, supported)
        val expected = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:2", "eip155:3"),
                methods = listOf("personal_sign", "eth_sendTransaction"),
                events = listOf("chainChanged"),
                accounts = listOf("eip155:1:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092", "eip155:2:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092", "eip155:3:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092")
            )
        )

        assertEquals(expected, approved)
    }

    @Test
    fun `generate approved namespaces - config 5 - multiple inline chains`() {
        val required = mapOf(
            "eip155:1" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged")),
            "eip155:2" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged"))
        )
        val optional = mapOf(
            "eip155" to Sign.Model.Namespace.Proposal(chains = listOf("eip155:3"), methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged")),
            "eip155:4" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged"))
        )
        val accounts = listOf(
            "eip155:1:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092",
            "eip155:2:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092",
            "eip155:3:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092",
            "eip155:4:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092"
        )
        val supported = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:2", "eip155:3", "eip155:4"),
                methods = listOf("personal_sign", "eth_sendTransaction", "eth_signTransaction"),
                events = listOf("chainChanged"),
                accounts = accounts
            )
        )
        val proposal = Sign.Model.SessionProposal("", "", "", "", listOf(), requiredNamespaces = required, optionalNamespaces = optional, mapOf(), "", "", "")

        val approved = generateApprovedNamespaces(proposal, supported)
        val expected = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:2", "eip155:3", "eip155:4"),
                methods = listOf("personal_sign", "eth_sendTransaction"),
                events = listOf("chainChanged"),
                accounts = accounts
            )
        )

        assertEquals(expected, approved)
    }

    @Test
    fun `generate approved namespaces - config 6 - unsupported optional chains`() {
        val required = mapOf(
            "eip155:1" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged")),
            "eip155:2" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged"))
        )
        val optional = mapOf(
            "eip155" to Sign.Model.Namespace.Proposal(chains = listOf("eip155:3"), methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged")),
            "eip155:4" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged"))
        )
        val accounts = listOf(
            "eip155:1:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092",
            "eip155:2:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092",
        )
        val supported = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf("personal_sign", "eth_sendTransaction", "eth_signTransaction"),
                events = listOf("chainChanged"),
                accounts = accounts
            )
        )
        val proposal = Sign.Model.SessionProposal("", "", "", "", listOf(), requiredNamespaces = required, optionalNamespaces = optional, mapOf(), "", "", "")

        val approved = generateApprovedNamespaces(proposal, supported)
        val expected = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf("personal_sign", "eth_sendTransaction"),
                events = listOf("chainChanged"),
                accounts = accounts
            )
        )

        assertEquals(expected, approved)
    }

    @Test
    fun `generate approved namespaces - config 7 - partially supported optional chains`() {
        val required = mapOf(
            "eip155:1" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged")),
            "eip155:2" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged"))
        )
        val optional = mapOf(
            "eip155" to Sign.Model.Namespace.Proposal(chains = listOf("eip155:3"), methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged")),
            "eip155:4" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged"))
        )
        val accounts = listOf(
            "eip155:1:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092",
            "eip155:2:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092",
            "eip155:4:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092"
        )
        val supported = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:2", "eip155:4"),
                methods = listOf("personal_sign", "eth_sendTransaction", "eth_signTransaction"),
                events = listOf("chainChanged"),
                accounts = accounts
            )
        )
        val proposal = Sign.Model.SessionProposal("", "", "", "", listOf(), requiredNamespaces = required, optionalNamespaces = optional, mapOf(), "", "", "")

        val approved = generateApprovedNamespaces(proposal, supported)
        val expected = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:2", "eip155:4"),
                methods = listOf("personal_sign", "eth_sendTransaction"),
                events = listOf("chainChanged"),
                accounts = accounts
            )
        )

        assertEquals(expected, approved)
    }

    @Test
    fun `generate approved namespaces - config 8 - partially supported optional methods`() {
        val required = mapOf(
            "eip155:1" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged")),
            "eip155:2" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged"))
        )
        val optional = mapOf(
            "eip155" to Sign.Model.Namespace.Proposal(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf("personal_sign", "eth_sendTransaction", "eth_signTransaction", "eth_signTypedData"),
                events = listOf("chainChanged")
            ),
        )
        val accounts = listOf(
            "eip155:1:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092",
            "eip155:2:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092"
        )
        val supported = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:2", "eip155:4"),
                methods = listOf("personal_sign", "eth_sendTransaction", "eth_signTransaction"),
                events = listOf("chainChanged"),
                accounts = accounts
            )
        )
        val proposal = Sign.Model.SessionProposal("", "", "", "", listOf(), requiredNamespaces = required, optionalNamespaces = optional, mapOf(), "", "", "")

        val approved = generateApprovedNamespaces(proposal, supported)
        val expected = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf("personal_sign", "eth_sendTransaction", "eth_signTransaction"),
                events = listOf("chainChanged"),
                accounts = accounts
            )
        )

        assertEquals(expected, approved)
    }

    @Test
    fun `generate approved namespaces - config 9 - partially supported optional events`() {
        val required = mapOf(
            "eip155:1" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged")),
            "eip155:2" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged"))
        )
        val optional = mapOf(
            "eip155" to Sign.Model.Namespace.Proposal(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf("personal_sign", "eth_sendTransaction", "eth_signTransaction", "eth_signTypedData"),
                events = listOf("chainChanged", "accountChanged")
            ),
        )
        val accounts = listOf(
            "eip155:1:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092",
            "eip155:2:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092"
        )
        val supported = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:2", "eip155:4"),
                methods = listOf("personal_sign", "eth_sendTransaction", "eth_signTransaction"),
                events = listOf("chainChanged", "accountChanged"),
                accounts = accounts
            )
        )
        val proposal = Sign.Model.SessionProposal("", "", "", "", listOf(), requiredNamespaces = required, optionalNamespaces = optional, mapOf(), "", "", "")

        val approved = generateApprovedNamespaces(proposal, supported)
        val expected = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf("personal_sign", "eth_sendTransaction", "eth_signTransaction"),
                events = listOf("chainChanged", "accountChanged"),
                accounts = accounts
            )
        )

        assertEquals(expected, approved)
    }

    @Test
    fun `generate approved namespaces - config 10 - extra supported chains`() {
        val required = mapOf(
            "eip155:1" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged")),
            "eip155:2" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged"))
        )
        val optional = mapOf(
            "eip155" to Sign.Model.Namespace.Proposal(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf("personal_sign", "eth_sendTransaction", "eth_signTransaction", "eth_signTypedData"),
                events = listOf("chainChanged", "accountChanged")
            ),
        )
        val accounts = listOf(
            "eip155:1:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092",
            "eip155:2:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092",
            "eip155:4:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092"
        )
        val supported = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:2", "eip155:4"),
                methods = listOf("personal_sign", "eth_sendTransaction", "eth_signTransaction"),
                events = listOf("chainChanged", "accountChanged"),
                accounts = accounts
            )
        )
        val proposal = Sign.Model.SessionProposal("", "", "", "", listOf(), requiredNamespaces = required, optionalNamespaces = optional, mapOf(), "", "", "")

        val approved = generateApprovedNamespaces(proposal, supported)
        val expected = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf("personal_sign", "eth_sendTransaction", "eth_signTransaction"),
                events = listOf("chainChanged", "accountChanged"),
                accounts = listOf(
                    "eip155:1:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092",
                    "eip155:2:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092"
                )
            )
        )

        assertEquals(expected, approved)
    }

    @Test
    fun `generate approved namespaces - config 11 - multiple required namespaces`() {
        val required = mapOf(
            "eip155:1" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged")),
            "cosmos" to Sign.Model.Namespace.Proposal(chains = listOf("cosmos:cosmoshub-4"), methods = listOf("cosmos_method"), events = listOf("cosmos_event"))
        )
        val optional = mapOf(
            "eip155" to Sign.Model.Namespace.Proposal(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf(
                    "personal_sign",
                    "eth_sendTransaction",
                    "eth_signTransaction",
                    "eth_signTypedData",
                ),
                events = listOf("chainChanged", "accountChanged")
            ),
        )
        val eipAccounts = listOf(
            "eip155:1:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092",
            "eip155:2:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092",
            "eip155:4:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092"
        )
        val cosmosAccounts = listOf("cosmos:cosmoshub-4:cosmos1hsk6jryyqjfhp5dhc55tc9jtckygx0eph6dd02")
        val supported = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:2", "eip155:4"),
                methods = listOf("personal_sign", "eth_sendTransaction", "eth_signTransaction"),
                events = listOf("chainChanged", "accountChanged"),
                accounts = eipAccounts
            ),
            "cosmos" to Sign.Model.Namespace.Session(
                chains = listOf("cosmos:cosmoshub-4"),
                methods = listOf("cosmos_method"),
                events = listOf("cosmos_event"),
                accounts = cosmosAccounts
            )
        )
        val proposal = Sign.Model.SessionProposal("", "", "", "", listOf(), requiredNamespaces = required, optionalNamespaces = optional, mapOf(), "", "", "")

        val approved = generateApprovedNamespaces(proposal, supported)
        val expected = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf("personal_sign", "eth_sendTransaction", "eth_signTransaction"),
                events = listOf("chainChanged", "accountChanged"),
                accounts = listOf(
                    "eip155:1:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092",
                    "eip155:2:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092"
                )
            ),
            "cosmos" to Sign.Model.Namespace.Session(
                chains = listOf("cosmos:cosmoshub-4"),
                methods = listOf("cosmos_method"),
                events = listOf("cosmos_event"),
                accounts = cosmosAccounts
            )
        )

        assertEquals(expected, approved)
    }

    @Test
    fun `should throw error - config 1 - required chains are not supported`() {
        val required = mapOf(
            "eip155:1" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged")),
            "cosmos" to Sign.Model.Namespace.Proposal(chains = listOf("cosmos:cosmoshub-4"), methods = listOf("cosmos_method"), events = listOf("cosmos_event"))
        )
        val optional = mapOf(
            "eip155" to Sign.Model.Namespace.Proposal(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf(
                    "personal_sign",
                    "eth_sendTransaction",
                    "eth_signTransaction",
                    "eth_signTypedData",
                ),
                events = listOf("chainChanged", "accountChanged")
            ),
        )
        val eipAccounts = listOf("eip155:5:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092")
        val supported = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:5"),
                methods = listOf("personal_sign", "eth_sendTransaction", "eth_signTransaction"),
                events = listOf("chainChanged", "accountChanged"),
                accounts = eipAccounts
            )
        )
        val proposal = Sign.Model.SessionProposal("", "", "", "", listOf(), requiredNamespaces = required, optionalNamespaces = optional, mapOf(), "", "", "")
        val exception = assertThrows<Exception> { generateApprovedNamespaces(proposal, supported) }
        assertEquals("All required namespaces must be approved", "${exception.message}")
    }

    @Test
    fun `should throw error - config 2 - partially supported required chains`() {
        val required = mapOf(
            "eip155:1" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged")),
            "cosmos" to Sign.Model.Namespace.Proposal(chains = listOf("cosmos:cosmoshub-4"), methods = listOf("cosmos_method"), events = listOf("cosmos_event"))
        )
        val optional = mapOf(
            "eip155" to Sign.Model.Namespace.Proposal(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf(
                    "personal_sign",
                    "eth_sendTransaction",
                    "eth_signTransaction",
                    "eth_signTypedData",
                ),
                events = listOf("chainChanged", "accountChanged")
            ),
        )
        val eipAccounts = listOf("eip155:1:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092", "eip155:5:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092")
        val supported = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:5"),
                methods = listOf("personal_sign", "eth_sendTransaction", "eth_signTransaction"),
                events = listOf("chainChanged", "accountChanged"),
                accounts = eipAccounts
            )
        )
        val proposal = Sign.Model.SessionProposal("", "", "", "", listOf(), requiredNamespaces = required, optionalNamespaces = optional, mapOf(), "", "", "")
        val exception = assertThrows<Exception> { generateApprovedNamespaces(proposal, supported) }
        assertEquals("All required namespaces must be approved", "${exception.message}")
    }

    @Test
    fun `should throw error - config 3 - not supported required methods`() {
        val required = mapOf("eip155:1" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged")))
        val optional = mapOf(
            "eip155" to Sign.Model.Namespace.Proposal(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf(
                    "personal_sign",
                    "eth_sendTransaction",
                    "eth_signTransaction",
                    "eth_signTypedData",
                ),
                events = listOf("chainChanged", "accountChanged")
            ),
        )
        val eipAccounts = listOf("eip155:1:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092")
        val supported = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1"),
                methods = listOf("personal_sign"),
                events = listOf("chainChanged", "accountChanged"),
                accounts = eipAccounts
            )
        )
        val proposal = Sign.Model.SessionProposal("", "", "", "", listOf(), requiredNamespaces = required, optionalNamespaces = optional, mapOf(), "", "", "")
        val exception = assertThrows<Exception> { generateApprovedNamespaces(proposal, supported) }
        assertEquals("All required namespaces must be approved: not all methods are approved", "${exception.message}")
    }

    @Test
    fun `should throw error - config 4 - not supported required methods`() {
        val required = mapOf("eip155:1" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged")))
        val optional = mapOf(
            "eip155" to Sign.Model.Namespace.Proposal(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf(
                    "personal_sign",
                    "eth_sendTransaction",
                    "eth_signTransaction",
                    "eth_signTypedData",
                ),
                events = listOf("chainChanged", "accountChanged")
            ),
        )
        val eipAccounts = listOf("eip155:1:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092")
        val supported = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1"),
                methods = listOf("personal_sign"),
                events = listOf(),
                accounts = eipAccounts
            )
        )
        val proposal = Sign.Model.SessionProposal("", "", "", "", listOf(), requiredNamespaces = required, optionalNamespaces = optional, mapOf(), "", "", "")
        val exception = assertThrows<Exception> { generateApprovedNamespaces(proposal, supported) }
        assertEquals("All required namespaces must be approved: not all methods are approved", "${exception.message}")
    }

    @Test
    fun `should throw error - config 5 - no accounts for required chains`() {
        val required = mapOf("eip155:1" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged")))
        val optional = mapOf(
            "eip155" to Sign.Model.Namespace.Proposal(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf(
                    "personal_sign",
                    "eth_sendTransaction",
                    "eth_signTransaction",
                    "eth_signTypedData",
                ),
                events = listOf("chainChanged", "accountChanged")
            ),
        )
        val eipAccounts = listOf("eip155:2:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092")
        val supported = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1"),
                methods = listOf("personal_sign", "eth_sendTransaction"),
                events = listOf("chainChanged"),
                accounts = eipAccounts
            )
        )
        val proposal = Sign.Model.SessionProposal("", "", "", "", listOf(), requiredNamespaces = required, optionalNamespaces = optional, mapOf(), "", "", "")
        val exception = assertThrows<Exception> { generateApprovedNamespaces(proposal, supported) }
        assertEquals("Accounts must be defined in matching namespace", "${exception.message}")
    }

    @Test
    fun `should throw error - config 6 - partial accounts for required chains`() {
        val required = mapOf(
            "eip155:1" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged")),
            "eip155:2" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged"))
        )
        val optional = mapOf(
            "eip155" to Sign.Model.Namespace.Proposal(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf(
                    "personal_sign",
                    "eth_sendTransaction",
                    "eth_signTransaction",
                    "eth_signTypedData",
                ),
                events = listOf("chainChanged", "accountChanged")
            ),
        )
        val eipAccounts = listOf("eip155:2:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092")
        val supported = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf("personal_sign", "eth_sendTransaction"),
                events = listOf("chainChanged"),
                accounts = eipAccounts
            )
        )
        val proposal = Sign.Model.SessionProposal("", "", "", "", listOf(), requiredNamespaces = required, optionalNamespaces = optional, mapOf(), "", "", "")
        val exception = assertThrows<Exception> { generateApprovedNamespaces(proposal, supported) }
        assertEquals("Accounts must be defined in matching namespace", "${exception.message}")
    }

    @Test
    fun `should throw error - config 7 - caip-10 is not supported`() {
        val required = mapOf(
            "eip155:1" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged")),
            "eip155:2" to Sign.Model.Namespace.Proposal(methods = listOf("eth_sendTransaction", "personal_sign"), events = listOf("chainChanged"))
        )
        val optional = mapOf(
            "eip155" to Sign.Model.Namespace.Proposal(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf(
                    "personal_sign",
                    "eth_sendTransaction",
                    "eth_signTransaction",
                    "eth_signTypedData",
                ),
                events = listOf("chainChanged", "accountChanged")
            ),
        )
        val eipAccounts = listOf("eip155:2:0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092", "0x57f48fAFeC1d76B27e3f29b8d277b6218CDE6092")
        val supported = mapOf(
            "eip155" to Sign.Model.Namespace.Session(
                chains = listOf("eip155:1", "eip155:2"),
                methods = listOf("personal_sign", "eth_sendTransaction"),
                events = listOf("chainChanged"),
                accounts = eipAccounts
            )
        )
        val proposal = Sign.Model.SessionProposal("", "", "", "", listOf(), requiredNamespaces = required, optionalNamespaces = optional, mapOf(), "", "", "")
        val exception = assertThrows<Exception> { generateApprovedNamespaces(proposal, supported) }
        assertEquals("Accounts must be CAIP-10 compliant", "${exception.message}")
    }
}