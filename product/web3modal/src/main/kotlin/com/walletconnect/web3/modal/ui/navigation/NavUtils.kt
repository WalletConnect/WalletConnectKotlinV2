package com.walletconnect.web3.modal.ui.navigation

import androidx.navigation.NavBackStackEntry

private const val TITLE_KEY = "title"
private const val TITLE_ARG = "{title}"

internal fun NavBackStackEntry.getTitleArg() = arguments?.getString(TITLE_KEY)

internal fun addTitleArg() = "&$TITLE_ARG"
