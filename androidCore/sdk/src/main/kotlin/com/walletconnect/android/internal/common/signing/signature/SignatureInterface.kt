@file:Suppress("PackageDirectoryMismatch")

package com.walletconnect.android.cacao

interface SignatureInterface {
    val t: String
    val s: String
    val m: String?
}