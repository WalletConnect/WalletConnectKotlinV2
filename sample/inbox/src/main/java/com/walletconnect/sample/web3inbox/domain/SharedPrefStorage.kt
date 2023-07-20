package com.walletconnect.sample.web3inbox.domain

import android.content.Context

object SharedPrefStorage {
    fun getShouldRemember(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(SHOULD_REMEMBER, true)
    }

    fun saveShouldRemember(context: Context, state: Boolean) {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.edit().putBoolean(SHOULD_REMEMBER, state).apply()
    }

    fun getLastLoggedInAccount(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(LAST_LOGGED_IN_ACCOUNT, null)
    }

    fun saveLastLoggedInAccount(context: Context, account: String) {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(LAST_LOGGED_IN_ACCOUNT, account).apply()
    }

    fun removeLastLoggedInAccount(context: Context) {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().remove(LAST_LOGGED_IN_ACCOUNT).apply()
    }

    private const val SHOULD_REMEMBER = "should_remember"
    private const val LAST_LOGGED_IN_ACCOUNT = "last_logged_in_account"
    private const val SHARED_PREF_NAME = "W3I_Sample_Shared_Pref"

    private const val ACCOUNT_TAG = "self_account_tag"
    private const val PRIVATE_KEY_TAG = "self_private_key"
    private const val PUBLIC_KEY_TAG = "self_public_key"

    fun isInitialized(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return (sharedPreferences.getString(ACCOUNT_TAG, null) != null) && (sharedPreferences.getString(PRIVATE_KEY_TAG, null) != null) && (sharedPreferences.getString(PUBLIC_KEY_TAG, null) != null)
    }

    fun getSavedRandomAccount(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(ACCOUNT_TAG, null)
    }

    fun getSavedRandomPrivateKey(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(PRIVATE_KEY_TAG, null)
    }

    fun getSavedRandomPublicKey(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(PUBLIC_KEY_TAG, null)
    }

    fun saveRandomAccount(context: Context, account: String) {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(ACCOUNT_TAG, account).apply()
    }

    fun saveRandomPrivateKey(context: Context, privateKey: String) {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(PRIVATE_KEY_TAG, privateKey).apply()
    }

    fun saveRandomPublicKey(context: Context, publicKey: String) {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(PUBLIC_KEY_TAG, publicKey).apply()
    }
}