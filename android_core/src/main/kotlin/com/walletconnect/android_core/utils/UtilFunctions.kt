@file:Suppress("PackageDirectoryMismatch")

package com.walletconnect.utils

@get:JvmSynthetic
internal val String.Companion.Empty get() = ""

fun Long.extractTimestamp() = this / 1000
