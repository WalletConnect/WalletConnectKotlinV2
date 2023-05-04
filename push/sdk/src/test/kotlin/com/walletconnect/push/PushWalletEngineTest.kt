package com.walletconnect.push

import org.json.JSONObject

import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels

internal class PushWalletEngineTest {
//    val pushWalletEngine = PushWalletEngine(
//        keyserverUrl = mockk(),
//        jsonRpcInteractor = mockk(),
//        crypto = mockk(),
//        pairingHandler = mockk(),
//        subscriptionStorageRepository = mockk(),
//        messageRepository = mockk(),
//        identitiesInteractor = mockk(),
//        serializer = mockk(),
//        logger = mockk()
//    )

    @Test
    fun subscribe() {
        val dappURL = URL("https://gm.walletconnect.com/.well-known/did.json")
        val wellKnownDidJson = File("wellKnownDid.json")

        dappURL.openStream().use { dappUrlIs ->
            Channels.newChannel(dappUrlIs).use { rbc ->
                FileOutputStream(wellKnownDidJson).use { fos ->
                    fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
                }
            }
        }

        val wellKnownDidJsonString = wellKnownDidJson.bufferedReader().use { it.readText() }
        println(wellKnownDidJsonString)
        val t = JSONObject(wellKnownDidJsonString)
        println(t)
        val keyAgreement = t.getJSONArray("keyAgreement").getString(0)
        println(keyAgreement)
        val verificationMethodArray = t.getJSONArray("verificationMethod")
        val verificationMethodList = (0 until verificationMethodArray.length()).map { verificationMethodArray.getJSONObject(it) }
        val keyObject = verificationMethodList.first { it.getString("id") == keyAgreement }
        val publicKeyJwk = keyObject.getJSONObject("publicKeyJwk")
        println(publicKeyJwk)
    }
}