package com.walletconnect.web3.modal.ui.routes.connect

import androidx.lifecycle.ViewModel
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.web3.modal.domain.magic.handler.MagicController
import com.walletconnect.web3.modal.ui.navigation.Navigator
import com.walletconnect.web3.modal.ui.navigation.NavigatorImpl

class EmailViewModel : ViewModel(), Navigator by NavigatorImpl() {

    private val magicController: MagicController = wcKoinApp.koin.get()

}