package com.walletconnect.web3.modal.ui.components.internal.commons

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix

internal val grayColorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
