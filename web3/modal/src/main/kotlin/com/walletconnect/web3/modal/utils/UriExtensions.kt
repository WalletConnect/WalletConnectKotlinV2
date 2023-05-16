package com.walletconnect.web3.modal.utils

import androidx.core.net.toUri

internal fun String.toDeeplinkUri() = replace("wc:", "wc://").toUri()
