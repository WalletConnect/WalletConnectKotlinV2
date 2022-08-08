package com.walletconnect.foundation.util

fun generateId(): Long = (System.currentTimeMillis() + (100..999).random())