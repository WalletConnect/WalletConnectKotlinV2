package com.walletconnect.sign

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Namespace
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SessionProposer
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import junit.framework.TestCase.assertEquals
import org.junit.Test

class AttestationIdGenerationTest {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    data class MockRequest(
        val id: Long = 1681755924133124,
        val jsonrpc: String = "2.0",
        val method: String = "wc_sessionRequest",
        val params: MockParams = MockParams()
    )

    data class MockParams(val payload: List<String> = listOf())

    @Test
    fun `generate attestation id test`() {
        val result = sha256("some".toByteArray())
        assertEquals("a6b46dd0d1ae5e86cbc8f37e75ceeb6760230c1ca4ffbcb0c97b96dd7d9c464b", result)
    }

    @Test
    fun `generate attestation id from mocked request payload test`() {
        val mockRequest = MockRequest()
        val json = moshi.adapter(MockRequest::class.java).toJson(mockRequest)
        val result = sha256(json.toByteArray())
        assertEquals("5d9847b41c77213c8dce1a227304dbbfd3bda90c776d368c9d9e8ac04e854512", result)
    }

    @Test
    fun `generate attestation id from session proposal payload test`() {
        val params =
            SignParams.SessionProposeParams(
                requiredNamespaces = mapOf(
                    "eip155" to Namespace.Proposal(
                        chains = listOf("eip155:1", "eip155:42161"),
                        methods = listOf("eth_sendTransaction", "eth_signTransaction", "personal_sign", "eth_signTypedData"),
                        events = listOf("chainChanged", "accountsChanged")
                    )
                ),
                optionalNamespaces = mapOf(
                    "polkadot" to Namespace.Proposal(
                        chains = listOf("polkadot:91b171bb158e2d3848fa23a9f1c25182"),
                        methods = listOf("polkadot_signTransaction", "polkadot_signMessage"),
                        events = listOf("chainChanged", "accountsChanged")
                    )
                ),
                relays = listOf(RelayProtocolOptions()),
                proposer = SessionProposer(
                    publicKey = "c90d2a6c2e693c05525cfd38188cf1ca0d4cd9fa52abcb3898abf52e61221120",
                    metadata = AppMetaData(
                        description = "Description of Proposer App run by client A",
                        url = "https://walletconnect.com",
                        name = "App A (Proposer)",
                        icons = listOf("https://avatars.githubusercontent.com/u/37784886")
                    )
                ),
                properties = mapOf("expiry" to "2022-12-24T17:07:31+00:00", "caip154-mandatory" to "true"),
                expiry = 123456789
            )

        val sessionPropose = SignRpc.SessionPropose(id = 1681757953038968, params = params)
        val json = moshi.adapter(SignRpc.SessionPropose::class.java).toJson(sessionPropose)
        val result = sha256(json.toByteArray())
        assertEquals("3948c03c15d5881118e0f3ace865199163c18cc23b0cf1e72812536c2e76de92", result)
    }

    @Test
    fun `generate attestation id from session proposal payload test 1`() {
        val params =
            SignParams.SessionProposeParams(
                requiredNamespaces = mapOf(
                    "eip155" to Namespace.Proposal(
                        chains = listOf("eip155:1"),
                        methods = listOf("eth_sendTransaction", "eth_signTransaction", "eth_sign", "personal_sign", "eth_signTypedData"),
                        events = listOf("chainChanged", "accountsChanged")
                    )
                ),
                optionalNamespaces = emptyMap(),
                relays = listOf(RelayProtocolOptions()),
                proposer = SessionProposer(
                    publicKey = "582554302bfc374c5008315526b8d533f3cffd032b50ff487b537f7e3009f13d",
                    metadata = AppMetaData(
                        name = "React App",
                        description = "React App for WalletConnect",
                        url = "https://react-app.walletconnect.com",
                        icons = listOf("https://avatars.githubusercontent.com/u/37784886")
                    )
                ),
                properties = null,
                expiry = 123456789
            )

        val sessionPropose = SignRpc.SessionPropose(id = 1681824460577019, params = params)
        val json = moshi.adapter(SignRpc.SessionPropose::class.java).toJson(sessionPropose)
        val result = sha256(json.toByteArray())
        assertEquals("cad66bf51d3cb0de784b916503212389de272109bbe29e6efe78d989f38171ae", result)
    }
}