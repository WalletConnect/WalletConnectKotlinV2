@file:Suppress("PackageDirectoryMismatch")

package com.walletconnect.util

fun generateId(): Long = (System.currentTimeMillis() + (100..999).random())