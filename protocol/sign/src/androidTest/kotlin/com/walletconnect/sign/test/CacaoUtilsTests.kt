package com.walletconnect.sign.test

import com.walletconnect.android.internal.common.signing.cacao.decodeReCaps
import com.walletconnect.android.internal.common.signing.cacao.getChains
import com.walletconnect.android.internal.common.signing.cacao.getMethods
import com.walletconnect.android.internal.common.signing.cacao.parseReCaps
import org.junit.Test

class CacaoUtilsTests {

    @Test
    fun decodeWhenReCapsIsNotTheLastElementShouldBeEmptyTest() {
        val resources = listOf(
            "urn:recap:eyJhdHQiOnsiaHR0cHM6Ly9ub3RpZnkud2FsbGV0Y29ubmVjdC5jb20vYWxsLWFwcHMiOnsiY3J1ZC9zdWJzY3JpcHRpb25zIjpbe31dLCJjcnVkL25vdGlmaWNhdGlvbnMiOlt7fV19fX0=",
            "ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/"
        )

        resources.decodeReCaps().also {
            assert(it == null)
        }
    }

    @Test
    fun decodeWhenReCapsIsTheLastElementShouldBeNotEmptyTest() {
        val resources = listOf(
            "ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/",
            "urn:recap:eyJhdHQiOnsiZWlwMTU1Ijp7InJlcXVlc3RcL3BlcnNvbmFsX3NpZ24iOlt7fV0sInJlcXVlc3RcL2V0aF9zaWduVHlwZWREYXRhIjpbe31dfSwiaHR0cHM6XC9cL25vdGlmeS53YWxsZXRjb25uZWN0LmNvbVwvYWxsLWFwcHMiOnsiY3J1ZFwvc3Vic2NyaXB0aW9ucyI6W3t9XSwiY3J1ZFwvbm90aWZpY2F0aW9ucyI6W3t9XX19fQ",
        )

        resources.decodeReCaps().also {
            assert(it != null)
            assert(it?.contains("att") == true)
        }
    }

    @Test
    fun parseReCapsFromJSONToMapTest() {
        val resources = listOf(
            "ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/",
            "urn:recap:eyJhdHQiOnsiaHR0cHM6Ly9ub3RpZnkud2FsbGV0Y29ubmVjdC5jb20vYWxsLWFwcHMiOnsiY3J1ZC9zdWJzY3JpcHRpb25zIjpbe31dLCJjcnVkL25vdGlmaWNhdGlvbnMiOlt7fV19fX0=",
        )

        resources.decodeReCaps().parseReCaps().also {
            println(it)
            assert(it.isNotEmpty())
            assert(it.keys.contains("https://notify.walletconnect.com/all-apps"))
            assert(it.values.size == 1)
        }
    }

    @Test
    fun getMethodsFromNonSignReCapsShouldBeEmpty(){
        val resources = listOf(
            "ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/",
            "urn:recap:eyJhdHQiOnsiaHR0cHM6Ly9ub3RpZnkud2FsbGV0Y29ubmVjdC5jb20vYWxsLWFwcHMiOnsiY3J1ZC9zdWJzY3JpcHRpb25zIjpbe31dLCJjcnVkL25vdGlmaWNhdGlvbnMiOlt7fV19fX0=",
        )

        resources.getMethods().also {
            assert(it.isEmpty())
        }
    }

    @Test
    fun getMethodsFromSignReCapsShouldNotBeEmpty(){
        val resources = listOf(
            "ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/",
            "urn:recap:eyJhdHQiOnsiZWlwMTU1Ijp7InJlcXVlc3QvcGVyc29uYWxfc2lnbiI6W3siY2hhaW5zIjpbImVpcDE1NToxIl19XSwicmVxdWVzdC9ldGhfc2lnblR5cGVkRGF0YV92NCI6W3siY2hhaW5zIjpbImVpcDE1NToxIl19XX19fQ",
        )

        resources.getMethods().also {
            assert(it.isNotEmpty())
            assert(it.contains("personal_sign"))
        }
    }

    @Test
    fun getChainsFromNonSignReCapsShouldBeEmpty(){
        val resources = listOf(
            "ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/",
            "urn:recap:eyJhdHQiOnsiaHR0cHM6Ly9ub3RpZnkud2FsbGV0Y29ubmVjdC5jb20vYWxsLWFwcHMiOnsiY3J1ZC9zdWJzY3JpcHRpb25zIjpbe31dLCJjcnVkL25vdGlmaWNhdGlvbnMiOlt7fV19fX0=",
        )

        resources.getChains().also {
            assert(it.isEmpty())
        }
    }

    @Test
    fun getChainsFromSignReCapsWithChainsShouldNotBeEmpty(){
        val resources = listOf(
            "ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/",
            "urn:recap:eyJhdHQiOnsiZWlwMTU1Ijp7InJlcXVlc3QvcGVyc29uYWxfc2lnbiI6W3siY2hhaW5zIjpbImVpcDE1NToxMzciLCJlaXAxNTU6NTYiXX1dfX19",
        )

        resources.getChains().also {
            assert(it.isNotEmpty())
            assert(it.contains("eip155:137"))
        }
    }
}