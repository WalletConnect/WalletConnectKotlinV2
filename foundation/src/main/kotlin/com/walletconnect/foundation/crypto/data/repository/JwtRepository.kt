package com.walletconnect.foundation.crypto.data.repository

interface JwtRepository {

    fun generateJWT(serverUrl: String, getIssuer: (String) -> Unit = {}): String
}