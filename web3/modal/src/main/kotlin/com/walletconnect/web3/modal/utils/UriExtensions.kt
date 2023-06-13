package com.walletconnect.web3.modal.utils

import androidx.core.net.toUri

internal fun String.toNativeDeeplinkUri() = replace("?", "&").replace(":", "?uri=")

internal fun String.toDeeplinkUri() = replace("wc:", "wc://").toUri()