package com.walletconnect.sign.util

import com.walletconnect.sign.client.utils.normalizeNamespaces
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

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
}