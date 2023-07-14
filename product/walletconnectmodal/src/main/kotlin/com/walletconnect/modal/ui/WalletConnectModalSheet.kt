@file:OptIn(ExperimentalAnimationApi::class)

package com.walletconnect.modal.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentDialog
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class WalletConnectModalSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ModalComposeView()
            }
        }
    }

    @Composable
    private fun ModalComposeView() {
        val navController = rememberAnimatedNavController()
        (dialog as? ComponentDialog)?.onBackPressedDispatcher?.addCallback(
            this@WalletConnectModalSheet,
            onBackPressedCallback(navController)
        )
        WalletConnectModalComponent(
            navController = navController,
            closeModal = { this@WalletConnectModalSheet.dismiss() }
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
