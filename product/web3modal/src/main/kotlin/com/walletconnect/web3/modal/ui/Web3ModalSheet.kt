@file:OptIn(ExperimentalAnimationApi::class)

package com.walletconnect.web3.modal.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentDialog
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.unit.dp
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.use
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.walletconnect.modal.utils.theme.toComposeColor
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.components.internal.Web3ModalComponent
import com.walletconnect.web3.modal.ui.theme.ColorPalette

@Deprecated("com.walletconnect.web3.modal.ui.Web3ModalSheet has been deprecated. Please use com.reown.appkit.modal.ui.AppKitSheet instead from - https://github.com/reown-com/reown-kotlin")
class Web3ModalSheet : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        requireContext().setTheme(R.style.Web3ModalTheme_DialogTheme)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val shouldOpenChooseNetwork = arguments.getShouldOpenChooseNetworkArg()

        val mode = requireContext().getThemeMode()
        val colors = requireContext().getColorMap()

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Web3ModalTheme(
                    mode = mode,
                    lightColors = colors.getLightModeColors(),
                    darkColors = colors.getDarkModeColors()
                ) {
                    Web3ModalComposeView(shouldOpenChooseNetwork)
                }
            }
        }
    }

    @Composable
    private fun Web3ModalComposeView(
        shouldOpenChooseNetwork: Boolean
    ) {
        val navController = rememberAnimatedNavController()
        dialog?.setupDialog(navController)

        Surface(shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)) {
            Web3ModalComponent(
                modifier = Modifier.nestedScroll(rememberNestedScrollInteropConnection()),
                navController = navController,
                shouldOpenChooseNetwork = shouldOpenChooseNetwork,
                closeModal = { this@Web3ModalSheet.dismiss() })
        }
    }

    private fun Dialog.setupDialog(navController: NavHostController) {
        (this as? ComponentDialog)?.onBackPressedDispatcher?.addCallback(
            this@Web3ModalSheet, onBackPressedCallback(navController)
        )
        findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun onBackPressedCallback(navController: NavHostController) = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (navController.popBackStack().not()) {
                dismiss()
            }
        }
    }
}

@Composable
internal fun Map<Int, Color?>.getLightModeColors(): Web3ModalTheme.Colors {
    val defaultColors = Web3ModalTheme.provideLightWeb3ModalColors()
    val (foreground, background) = provideColorPallets(defaultColors)
    return Web3ModalTheme.provideLightWeb3ModalColors(
        accent100 = get(R.attr.modalAccent100) ?: defaultColors.accent100,
        accent90 = get(R.attr.modalAccent90) ?: defaultColors.accent90,
        accent80 = get(R.attr.modalAccent80) ?: defaultColors.accent80,
        foreground = foreground,
        background = background,
        overlay = get(R.attr.modalGrayGlass) ?: defaultColors.grayGlass,
        success = get(R.attr.modalSuccess) ?: defaultColors.success,
        error = get(R.attr.modalError) ?: defaultColors.error
    )
}

@Composable
internal fun Map<Int, Color?>.getDarkModeColors(): Web3ModalTheme.Colors {
    val defaultColors = Web3ModalTheme.provideDarkWeb3ModalColor()
    val (foreground, background) = provideColorPallets(defaultColors)
    return Web3ModalTheme.provideDarkWeb3ModalColor(
        accent100 = get(R.attr.modalAccent100) ?: defaultColors.accent100,
        accent90 = get(R.attr.modalAccent90) ?: defaultColors.accent90,
        accent80 = get(R.attr.modalAccent80) ?: defaultColors.accent80,
        foreground = foreground,
        background = background,
        overlay = get(R.attr.modalGrayGlass) ?: defaultColors.grayGlass,
        success = get(R.attr.modalSuccess) ?: defaultColors.success,
        error = get(R.attr.modalError) ?: defaultColors.error
    )
}

private fun Map<Int, Color?>.provideColorPallets(defaultColors: Web3ModalTheme.Colors): Pair<ColorPalette, ColorPalette> {
    val foreground = ColorPalette(
        color100 = this[R.attr.modalForeground100] ?: defaultColors.foreground.color100,
        color125 = this[R.attr.modalForeground125] ?: defaultColors.foreground.color125,
        color150 = this[R.attr.modalForeground150] ?: defaultColors.foreground.color150,
        color175 = this[R.attr.modalForeground175] ?: defaultColors.foreground.color175,
        color200 = this[R.attr.modalForeground200] ?: defaultColors.foreground.color200,
        color225 = this[R.attr.modalForeground225] ?: defaultColors.foreground.color225,
        color250 = this[R.attr.modalForeground250] ?: defaultColors.foreground.color250,
        color275 = this[R.attr.modalForeground275] ?: defaultColors.foreground.color275,
        color300 = this[R.attr.modalForeground300] ?: defaultColors.foreground.color300,
    )
    val background = ColorPalette(
        color100 = this[R.attr.modalBackground100] ?: defaultColors.background.color100,
        color125 = this[R.attr.modalBackground125] ?: defaultColors.background.color125,
        color150 = this[R.attr.modalBackground150] ?: defaultColors.background.color150,
        color175 = this[R.attr.modalBackground175] ?: defaultColors.background.color175,
        color200 = this[R.attr.modalBackground200] ?: defaultColors.background.color200,
        color225 = this[R.attr.modalBackground225] ?: defaultColors.background.color225,
        color250 = this[R.attr.modalBackground250] ?: defaultColors.background.color250,
        color275 = this[R.attr.modalBackground275] ?: defaultColors.background.color275,
        color300 = this[R.attr.modalBackground300] ?: defaultColors.background.color300,
    )
    return (foreground to background)
}

private fun Int.toThemeMode(): Web3ModalTheme.Mode = when (this) {
    1 -> Web3ModalTheme.Mode.DARK
    2 -> Web3ModalTheme.Mode.LIGHT
    else -> Web3ModalTheme.Mode.AUTO
}

private val themeColorsAttributesMap = mapOf(
    0 to R.attr.modalAccent100,
    1 to R.attr.modalAccent90,
    2 to R.attr.modalAccent80,
    3 to R.attr.modalForeground100,
    4 to R.attr.modalForeground125,
    5 to R.attr.modalForeground150,
    6 to R.attr.modalForeground175,
    7 to R.attr.modalForeground200,
    8 to R.attr.modalForeground225,
    9 to R.attr.modalForeground250,
    10 to R.attr.modalForeground275,
    11 to R.attr.modalForeground300,
    12 to R.attr.modalBackground100,
    13 to R.attr.modalBackground125,
    14 to R.attr.modalBackground150,
    15 to R.attr.modalBackground175,
    16 to R.attr.modalBackground200,
    17 to R.attr.modalBackground225,
    18 to R.attr.modalBackground250,
    19 to R.attr.modalBackground275,
    20 to R.attr.modalBackground300,
    21 to R.attr.modalGrayGlass,
    22 to R.attr.modalSuccess,
    23 to R.attr.modalError,
)

internal fun Context.getColorMap() = obtainStyledAttributes(themeColorsAttributesMap.values.toIntArray()).use {
    themeColorsAttributesMap.keys.map { id ->
        themeColorsAttributesMap[id]!! to try {
            it.getColorOrThrow(id).toComposeColor()
        } catch (e: Exception) {
            null
        }
    }
}.toMap()

internal fun Context.getThemeMode() = obtainStyledAttributes(intArrayOf(R.attr.modalMode)).use { it.getInt(0, 0) }.toThemeMode()

private fun Bundle?.getShouldOpenChooseNetworkArg() = this?.getBoolean(CHOOSE_NETWORK_KEY) ?: false
