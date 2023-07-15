package com.walletconnect.android.internal

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
    fun `validate WC uri test`() {
        val validUri =
            "wc:7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9@2?relay-protocol=irn&symKey=587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303"

        Validator.validateWCUri("").apply { assertEquals(null, this) }
        Validator.validateWCUri(validUri).apply {
            assertNotNull(this)
            assertEquals("7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9", this!!.topic.value)
            assertEquals("irn", this!!.relay.protocol)
            assertEquals("587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303", this.symKey.keyAsHex)
            assertEquals("2", this.version)
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
        )

        assertEquals(uri.toAbsoluteString(), "wc:11112222244444@2?relay-protocol=irn&relay-data=teeestData&symKey=0x12321321312312312321")

        val uri2 = WalletConnectUri(
            Topic("11112222244444"),
            SymmetricKey("0x12321321312312312321"),
            RelayProtocolOptions("irn"),
        )

        assertEquals(uri2.toAbsoluteString(), "wc:11112222244444@2?relay-protocol=irn&symKey=0x12321321312312312321")
    }
}