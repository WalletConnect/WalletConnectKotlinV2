package com.walletconnect.walletconnectv2.crypto.codec

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.walletconnectv2.core.adapters.SubscriptionIdAdapter
import com.walletconnect.walletconnectv2.core.adapters.TopicAdapter
import com.walletconnect.walletconnectv2.core.adapters.TtlAdapter
import com.walletconnect.walletconnectv2.core.model.vo.*
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.after.PostSettlementPairingVO
import com.walletconnect.walletconnectv2.core.model.vo.payload.EncryptionPayloadVO
import com.walletconnect.walletconnectv2.relay.data.codec.AuthenticatedEncryptionCodec
import com.walletconnect.walletconnectv2.util.bytesToHex
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AuthenticatedEncryptionCodecTest {

    private val codec: AuthenticatedEncryptionCodec = AuthenticatedEncryptionCodec()

    @Test
    fun `Codec AES_256_CBC and Hmac_SHA256 authentication test`() {
        val sharedKey = SharedKey("94BA14D48AAF8E0D3FA13E94A73C8745136EB7C3D7BA6232E6512A78D6624A04")
        val message = "WalletConnect"

        val encryptedMessage =
            codec.encrypt(message, sharedKey, PublicKey("355957413b1693eea042918f8f346618bfdc29e9d00f2e6bbd702bc29c3e2e4d"))
        val payload = encryptedMessage.toEncryptionPayload()
        assertEquals(payload.publicKey, "355957413b1693eea042918f8f346618bfdc29e9d00f2e6bbd702bc29c3e2e4d")
        val text = codec.decrypt(payload, sharedKey)
        assertEquals(text, message)
    }

    @Test
    fun `Codec AES_256_CBC and Hmac_SHA256 invalid HMAC test`() {
        val sharedKey1 = SharedKey("94BA14D48AAF8E0D3FA13E94A73C8745136EB7C3D7BA6232E6512A78D6624A04")
        val sharedKey2 = SharedKey("95BA14D48AAF8E0D3FA13E94A73C8745136EB7C3D7BA6232E6512A78D6624A04")
        val message = "WalletConnect"

        val encryptedMessage =
            codec.encrypt(message, sharedKey1, PublicKey("355957413b1693eea042918f8f346618bfdc29e9d00f2e6bbd702bc29c3e2e4d"))
        val payload = encryptedMessage.toEncryptionPayload()
        assertEquals(payload.publicKey, "355957413b1693eea042918f8f346618bfdc29e9d00f2e6bbd702bc29c3e2e4d")

        try {
            codec.decrypt(payload, sharedKey2)
        } catch (e: Exception) {
            assertEquals("Invalid Hmac", e.message)
        }
    }

    @Test
    fun `get auth and hmac keys test`() {
        val sharedKey = "4b21b43b2e04dbe0105d250f5d72d5d9c28d8de202f240863e268e4ded9e9a6a"

        val decryptionKey = "c237bae5d78d52a6a718202fabfaae1cdfb83dd8a54b575c2e2f3e11fb67fa8b"
        val hmac = "50e98dc7a1013c3c38f76aaa80dd7ca6c4230a866298415f308c59d4285a6f48"
        val (decrypt, auth) = codec.getKeys(sharedKey)

        assertEquals(decrypt.bytesToHex(), decryptionKey)
        assertEquals(auth.bytesToHex(), hmac)
    }

    @Test
    fun `deserialize encrypted message to encryption payload`() {
        val hex =
            "ffbecf819a49a266b262309ad269ae4016ef8b8ef1f010d4447b7e089aac0b943d5e2ca94646ddcfa92f4e8e5778cc3e39e3e876dd95065c5899b95a98512664a8c77853c47d31c2e714e50018f3d1b525dbd2f76cde5bff8b261f343ecb3d956ad9e74819c8729fa1c77be4b5fb7d39ccc697bda421fb90d11315d828e79fca6a27316d3b09f14c7f3483b25b000820e7b64a75e5f59216e5f0ecbc4ec20c53664ad5e967026aa119a32a655e3ff3e110ca4c7e629b845b8ecf7ea6f296a79a6de3dc5794c3a51059bb08b09974501ffcf2d7fddafafd9f1b22e97b6abbb6bcd978a8a87341f33bc662c101947a06c72f6c7709a0a612f46fcd8b5fbce0bdd4c56ca330e6e2802fbf6e3830210f3c1b626863de93fd02857c615436e1b9dc7d36d45bbec8acfb24cd45c46946832d5a7cc20334fd7405dba997daf4725bc849450f197e7e9e2f5e20839ba1f77895b3cbccc279fdc0a9d40156a28ad2adcd6a8afc68f9735c4e7c22c49caf5150f243bab702a71699c9b26420668c81fc5b311488331a4456ba1baf619818b4ecfe6f6de8f80dc42a85c785aa78dd187e82faec549780051551335c651af10f89a3e37103e56a8ebf27f3054e4303a6bce88d7c082bfda897facfd952df5d3d6776370884cb04923c804c99059bb269fdbff3543d89648f39a7cc6fdad61ea0f24deeab420bc65dde6c7a6a3f5fe3775fe4a95a8bf8b70ae946696c808206baf119f0b3142d502c7ca0c102548a1263de2c04bde47aa1a716ae7b00959e300b56d6f0595d1588e07c618b914e3c76cb7d103cd8c6b91ed0aaadc2c129455c07905e5272ea4039660cb8e53a64101dae6e8737a082ac9a9b531a4cbc83e009c1722ca108a26bd193817392890b80cf519f2f14e1fc0e1b47d0b7da47d0635eace28e42456a222da5f2044895914a0b21568d49c222f55b114a558649f094012dbaaabd02ad1aae591d80b8754bb39964f4b9c235166b1ea5c80eb9870e90f073722926f823e5ca72714de10f6f4ed4072bfd3ffc4d32ec0e920edb404b7b1afa1f001d18948fe25562c9b8d52824a4fad20082f28a13e96b7277cb4e7a5ccbbf8095293892b2bac008fcee038765743fb9688abf8affd2477f7de90494ccbba94f6a88a0e0c215d5134b70f41f28754e1b236ab43ec65696fa182fa9525a70e7f42141ec38cfe57d26230b3d520ba2769517c9f8f43a161d38438079b967ab73835865b68a22d3cde7a37fccad1ee3f33ae13bb0f09b4b86ce2ee07823ba793a0fafee"

        val payload = hex.toEncryptionPayload()

        assertEquals(payload.iv.length, 32)
        assertEquals(payload.publicKey.length, 64)
        assertEquals(payload.mac.length, 64)

        val sharedKey = SharedKey("b426d6b8b7a57930cae8870179864849d6e89f1e8e801f7ca9a50bc2384ee043")
        val json = codec.decrypt(payload, sharedKey)


        val moshi =
            Moshi.Builder().addLast { type, annotations, moshi ->
                when (type.getRawType().name) {
                    SubscriptionIdVO::class.qualifiedName -> SubscriptionIdAdapter
                    TopicVO::class.qualifiedName -> TopicAdapter
                    TtlVO::class.qualifiedName -> TtlAdapter
                    else -> null
                }
            }.addLast(KotlinJsonAdapterFactory()).build()

        val request: PostSettlementPairingVO.PairingPayload? =
            moshi.adapter(PostSettlementPairingVO.PairingPayload::class.java).fromJson(json)

        assertEquals(
            request?.params?.request?.params?.proposer?.publicKey,
            "37d8c448a2241f21550329f451e8c1901e7dad5135ade604f1e106437843037f"
        )
    }
}

internal fun String.toEncryptionPayload(): EncryptionPayloadVO {
    val pubKeyStartIndex = EncryptionPayloadVO.ivLength
    val macStartIndex = pubKeyStartIndex + EncryptionPayloadVO.publicKeyLength
    val cipherTextStartIndex = macStartIndex + EncryptionPayloadVO.macLength

    val iv = this.substring(0, pubKeyStartIndex)
    val publicKey = this.substring(pubKeyStartIndex, macStartIndex)
    val mac = this.substring(macStartIndex, cipherTextStartIndex)
    val cipherText = this.substring(cipherTextStartIndex, this.length)

    return EncryptionPayloadVO(iv, publicKey, mac, cipherText)
}