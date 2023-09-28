package com.walletconnect.web3.modal.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentDialog
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.walletconnect.web3.modal.ui.components.internal.Web3ModalComponent

class Web3ModalSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val shouldOpenChooseNetwork = arguments.getShouldOpenChooseNetworkArg()

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Web3ModalComposeView(shouldOpenChooseNetwork)
            }
        }
    }

    @Composable
    private fun Web3ModalComposeView(
        shouldOpenChooseNetwork: Boolean
    ) {
        val navController = rememberNavController()
        (dialog as? ComponentDialog)?.onBackPressedDispatcher?.addCallback(
            this@Web3ModalSheet,
            onBackPressedCallback(navController)
        )
        Web3ModalComponent(
            navController = navController,
            shouldOpenChooseNetwork = shouldOpenChooseNetwork,
            closeModal = { this@Web3ModalSheet.dismiss() }
        )
    }


    private fun onBackPressedCallback(navController: NavHostController) =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController.popBackStack().not()) {
                    dismiss()
                }
            }
        }
}

private fun Bundle?.getShouldOpenChooseNetworkArg() = this?.getBoolean(CHOOSE_NETWORK_KEY) ?: false
