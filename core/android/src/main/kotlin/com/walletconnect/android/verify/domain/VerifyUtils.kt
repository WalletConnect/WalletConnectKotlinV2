package com.walletconnect.android.verify.domain

import com.walletconnect.android.internal.common.model.Validation
import com.walletconnect.utils.compareDomains

fun getValidation(metadataUrl: String, origin: String) = if (compareDomains(metadataUrl, origin)) Validation.VALID else Validation.INVALID