@file:OptIn(ExperimentalAnimationApi::class)

package com.walletconnect.wcmodal.ui

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
import androidx.navigation.compose.rememberNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.walletconnect.modal.utils.theme.themeColor
import com.walletconnect.wcmodal.R
import com.walletconnect.wcmodal.ui.theme.WalletConnectModalTheme

class WalletConnectModalSheet : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        requireContext().setTheme(R.style.WalletConnectModalTheme)
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val accentColor = requireContext().themeColor(R.attr.accentColor)
        val onAccentColor = requireContext().themeColor(R.attr.onAccentColor)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                WalletConnectModalTheme(
                    accentColor = accentColor,
                    onAccentColor = onAccentColor,
                ) {
                    ModalComposeView()
                }
            }
        }
    }

    @Composable
    private fun ModalComposeView() {
        val navController = rememberNavController()
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
