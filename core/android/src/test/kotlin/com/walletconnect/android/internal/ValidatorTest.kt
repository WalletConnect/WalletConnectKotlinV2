package com.walletconnect.android.internal

import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.common.model.WalletConnectUri
import com.walletconnect.foundation.common.model.Topic
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.Test

internal class ValidatorTest {

    @Test
    fun `validate with encoded uri query parameter`() {
        val validUri =
            "deeplink://wc?uri=wc%3A7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9%402%3Frelay-protocol%3Dirn%26symKey%3D587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303"
            //"kotlin-web3wallet://wc?uri=5521cc5969ab1fdc242648d69230534769b74dd770f5379284f67452e16c870a@2?relay-protocol=irn&expiryTimestamp=1706870519&methods=wc_sessionAuthenticate&symKey=e7a471121c212cc370c1a96f1f429b1dd57664f91e504837a19064a4989ec82f"

        Validator.validateWCUri("").apply { assertEquals(null, this) }
        Validator.validateWCUri(validUri).apply {
            assertNotNull(this)
            assertEquals("7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9", this!!.topic.value)
            assertEquals("irn", this!!.relay.protocol)
            assertEquals("587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303", this.symKey.keyAsHex)
            assertEquals("2", this.version)
        }
    }

    @Test
    fun `validate with decoded uri query parameter`() {
        val validUri =
            "deeplink://wc?uri=wc:7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9@2?relay-protocol=irn&symKey=587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303"

        Validator.validateWCUri("").apply { assertEquals(null, this) }
        Validator.validateWCUri(validUri).apply {
            assertNotNull(this)
            assertEquals("7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9", this!!.topic.value)
            assertEquals("irn", this!!.relay.protocol)
            assertEquals("587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303", this.symKey.keyAsHex)
            assertEquals("2", this.version)
        }
    }

    @Test
    fun `validate WC uri test`() {
        val validUri =
            "wc:7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9@2?relay-protocol=irn&symKey=587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303&expiryTimestamp=1705667684"

        Validator.validateWCUri("").apply { assertEquals(null, this) }
        Validator.validateWCUri(validUri).apply {
            assertNotNull(this)
            assertEquals("7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9", this!!.topic.value)
            assertEquals("irn", this!!.relay.protocol)
            assertEquals("587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303", this.symKey.keyAsHex)
            assertEquals("2", this.version)
            assertEquals(Expiry(1705667684L), expiry)
        }

        val noTopicInvalidUri =
            "wc:@2?relay-protocol=irn&symKey=587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303"
        Validator.validateWCUri(noTopicInvalidUri).apply { assertNull(this) }

        val noPrefixInvalidUri =
            "7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9@2?relay-protocol=irn&symKey=587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303"
        Validator.validateWCUri(noPrefixInvalidUri).apply { assertNull(this) }

        val noSymKeyInvalidUri =
            "wc:7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9@2?relay-protocol=irn&symKey="
        Validator.validateWCUri(noSymKeyInvalidUri).apply { assertNull(this) }

        val noProtocolTypeInvalidUri =
            "wc:7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9@2?relay-protocol=&symKey=587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303"
        Validator.validateWCUri(noProtocolTypeInvalidUri).apply { assertNull(this) }
    }

    @Test
    fun `validate WC uri test optional data field`() {
        val validUri =
            "wc:7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9@2?relay-protocol=irn&relay-data=testData&symKey=587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303"

        Validator.validateWCUri("").apply { assertEquals(null, this) }
        Validator.validateWCUri(validUri).apply {
            assertNotNull(this)
            assertEquals("7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9", this!!.topic.value)
            assertEquals("irn", this!!.relay.protocol)
            assertEquals("testData", this.relay.data)
            assertEquals("587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303", this.symKey.keyAsHex)
            assertEquals("2", this.version)
        }
    }

    @Test
    fun `parse walletconnect uri to absolute string`() {
        val uri = WalletConnectUri(
            Topic("11112222244444"),
            SymmetricKey("0x12321321312312312321"),
            RelayProtocolOptions("irn", "teeestData"),
            expiry = null,
            methods = "wc_method"
        )

        assertEquals(uri.toAbsoluteString(), "wc:11112222244444@2?relay-protocol=irn&relay-data=teeestData&methods=wc_method&symKey=0x12321321312312312321")

        val uri2 = WalletConnectUri(
            Topic("11112222244444"),
            SymmetricKey("0x12321321312312312321"),
            RelayProtocolOptions("irn"),
            expiry = Expiry(1705667684),
            methods = ""
        )

        assertEquals(uri2.toAbsoluteString(), "wc:11112222244444@2?relay-protocol=irn&expiryTimestamp=1705667684&symKey=0x12321321312312312321")
    }
}