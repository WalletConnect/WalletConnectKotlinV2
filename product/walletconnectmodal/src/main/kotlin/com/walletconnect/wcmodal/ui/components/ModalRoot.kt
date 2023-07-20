package com.walletconnect.wcmodal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.modal.ui.components.logo.WalletConnectLogo
import com.walletconnect.wcmodal.ui.theme.ModalTheme
import com.walletconnect.wcmodal.ui.theme.ProvideModalThemeComposition

@Composable
internal fun ModalRoot(
    navController: NavController,
    closeModal: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Bottom
    ) {
        ProvideModalThemeComposition {
            Column(
                modifier = Modifier.background(
                    color = ModalTheme.colors.main,
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WalletConnectLogo(modifier = Modifier.weight(1f), color = ModalTheme.colors.onMainColor)
                    QuestionMarkIconButton(navController)
                    Spacer(modifier = Modifier.width(16.dp))
                    CloseIconButton { closeModal() }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            ModalTheme.colors.background,
                            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                        )
                ) {
                    content()
                }
            }
        }
    }
}