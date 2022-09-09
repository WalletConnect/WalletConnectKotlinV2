package com.walletconnect.auth.signature

import com.walletconnect.auth.client.Auth
import com.walletconnect.util.bytesToHex
import com.walletconnect.utils.HexPrefix
import org.web3j.crypto.Sign

fun Sign.SignatureData.toSignature(): Signature = Signature(v, r, s)
fun Signature.toCacaoSignature(): String = String.HexPrefix + r.bytesToHex() + s.bytesToHex() + v.bytesToHex()
fun Signature.toSignatureData(): Sign.SignatureData = Sign.SignatureData(v, r, s)

const val ISO_8601_PATTERN = "uuuu-MM-dd'T'HH:mm:ssXXX"