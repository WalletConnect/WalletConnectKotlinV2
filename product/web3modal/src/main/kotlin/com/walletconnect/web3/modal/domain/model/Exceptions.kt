package com.walletconnect.web3.modal.domain.model

import com.walletconnect.web3.modal.client.Modal

object InvalidSessionException: Throwable("Session data is missing")

internal fun Throwable.toModalError() = Modal.Model.Error(this)