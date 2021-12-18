package com.walletconnect.walletconnectv2.util

private const val STRING_LENGTH = 64
private val CHAR_POOL: List<Char> = ('A'..'F') + ('0'..'9')

internal fun getRandom64ByteHexString() = (1..STRING_LENGTH).map { CHAR_POOL.random() }.joinToString("")