@file:JvmSynthetic

package com.walletconnect.notify.engine.domain

import android.net.Uri

internal class GenerateAppropriateUriUseCase {

    operator fun invoke(dappUri: Uri, path: String): Uri =
        if (dappUri.path?.contains(path) == false) {
            dappUri.buildUpon().appendPath(path).build()
        } else {
            dappUri
        }
}