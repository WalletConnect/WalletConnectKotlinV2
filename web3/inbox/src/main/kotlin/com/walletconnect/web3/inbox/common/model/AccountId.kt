@file:JvmSynthetic

package com.walletconnect.web3.inbox.common.model


@JvmInline
internal value class AccountId(val value: String) {
    fun address() = value.split(":").last()
}