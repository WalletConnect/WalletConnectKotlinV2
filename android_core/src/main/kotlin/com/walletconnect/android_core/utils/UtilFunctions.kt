@file:Suppress("PackageDirectoryMismatch")

package com.walletconnect.utils

@get:JvmSynthetic
val String.Empty: String
    get() = ""

fun Long.extractTimestamp() = this / 1000