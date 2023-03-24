package com.walletconnect.foundation.crypto.data.repository

interface ClientIdJwtRepository {

    fun generateJWT(serverUrl: String, getIssuerClientId: (String) -> Unit = {}): String
}