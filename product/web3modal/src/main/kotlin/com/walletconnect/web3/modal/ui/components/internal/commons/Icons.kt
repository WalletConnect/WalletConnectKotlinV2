package com.walletconnect.web3.modal.ui.components.internal.commons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.walletconnect.modal.ui.components.common.roundedClickable
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun BackArrowIcon(
    tint: Color = Web3ModalTheme.colors.foreground.color100,
    onClick: () -> Unit
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_chevron_left),
        contentDescription = ContentDescription.BACK_ARROW.description,
        tint = tint,
        modifier = Modifier
            .size(36.dp)
            .roundedClickable(onClick = onClick)
            .padding(10.dp),
    )
}

@Composable
internal fun QuestionMarkIcon(
    modifier: Modifier = Modifier,
    tint: Color = Web3ModalTheme.colors.foreground.color100
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_question_mark),
        contentDescription = ContentDescription.QUESTION_MARK.description,
        tint = tint,
        modifier = modifier
    )
}

@Composable
internal fun CloseIcon(
    modifier: Modifier = Modifier,
    tint: Color = Web3ModalTheme.colors.foreground.color100,
    onClick: () -> Unit
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_close),
        contentDescription = ContentDescription.CLOSE.description,
        tint = tint,
        modifier = modifier
            .size(36.dp)
            .roundedClickable(onClick = onClick)
            .padding(10.dp),
    )
}

@Composable
internal fun RetryIcon(
    tint: Color = Web3ModalTheme.colors.inverse100
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_retry),
        contentDescription = ContentDescription.RETRY.description,
        tint = tint,
        modifier = Modifier.size(12.dp),
    )
}

@Composable
internal fun DeclinedIcon() {
    Icon(
        imageVector = ImageVector.vectorResource(R.drawable.ic_close),
        tint = Web3ModalTheme.colors.error,
        contentDescription = ContentDescription.DECLINED.description,
        modifier = Modifier
            .size(20.dp)
            .background(Web3ModalTheme.colors.error.copy(alpha = .2f), shape = CircleShape)
            .padding(4.dp)
    )
}

@Composable
internal fun WalletIcon(
    tint: Color = Web3ModalTheme.colors.inverse100
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_wallet),
        contentDescription = ContentDescription.WALLET.description,
        modifier = Modifier.size(12.dp),
        tint = tint
    )
}

@Composable
internal fun ExternalIcon(
    tint: Color = Web3ModalTheme.colors.foreground.color200
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_external_link),
        contentDescription = ContentDescription.EXTERNAL_LINK.description,
        modifier = Modifier.size(12.dp),
        tint = tint
    )
}

@Composable
internal fun CopyIcon(
    modifier: Modifier = Modifier,
    tint: Color = Web3ModalTheme.colors.foreground.color250
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_copy),
        contentDescription = ContentDescription.COPY.description,
        modifier = modifier,
        tint = tint
    )
}

@Composable
internal fun CompassIcon(
    tint: Color = Web3ModalTheme.colors.foreground.color150
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_compass),
        contentDescription = ContentDescription.COMPASS.description,
        modifier = Modifier.size(14.dp),
        tint = tint
    )
}

@Composable
internal fun ChevronRightIcon(
    tint: Color = Web3ModalTheme.colors.foreground.color200
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_chevron_right),
        contentDescription = ContentDescription.CHEVRON_RIGHT.description,
        modifier = Modifier.size(14.dp),
        tint = tint
    )
}

@Composable
internal fun RecentTransactionIcon(
    tint: Color = Web3ModalTheme.colors.accent100
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_swap),
        contentDescription = ContentDescription.SWAP.description,
        modifier = Modifier
            .border(2.dp, Web3ModalTheme.colors.grayGlass02, shape = CircleShape)
            .padding(2.dp)
            .size(32.dp)
            .background(Web3ModalTheme.colors.grayGlass10, shape = CircleShape)
            .padding(8.dp),
        tint = tint
    )
}

@Composable
internal fun DisconnectIcon(
    tint: Color = Web3ModalTheme.colors.foreground.color200
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_disconnect),
        contentDescription = ContentDescription.DISCONNECT.description,
        modifier = Modifier
            .border(2.dp, Web3ModalTheme.colors.grayGlass05, shape = CircleShape)
            .padding(2.dp)
            .size(32.dp)
            .background(Web3ModalTheme.colors.grayGlass10, shape = CircleShape)
            .padding(8.dp),
        tint = tint
    )
}

@Composable
internal fun ScanQRIcon(
    tint: Color = Web3ModalTheme.colors.accent100,
    onClick: () -> Unit
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_scan_qr),
        contentDescription = ContentDescription.SCAN_QR.description,
        modifier = Modifier
            .roundedClickable { onClick() }
            .size(40.dp)
            .background(
                color = Web3ModalTheme.colors.accent10,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = Web3ModalTheme.colors.accent10,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp),
        tint = tint
    )
}

@Composable
internal fun AllWalletsIcon(
    tint: Color = Web3ModalTheme.colors.accent100,
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_all_wallets),
        contentDescription = ContentDescription.SCAN_QR.description,
        modifier = Modifier
            .size(40.dp)
            .background(
                color = Web3ModalTheme.colors.accent10,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = Web3ModalTheme.colors.accent10,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp),
        tint = tint
    )
}

@Composable
internal fun SelectNetworkIcon(
    tint: Color = Web3ModalTheme.colors.foreground.color100
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_select_network),
        contentDescription = ContentDescription.SELECT_NETWORK.description,
        modifier = Modifier
            .size(24.dp)
            .background(
                color = Web3ModalTheme.colors.grayGlass25,
                shape = CircleShape
            )
            .padding(5.dp)
            .border(
                width = 1.dp,
                color = Web3ModalTheme.colors.grayGlass10,
                shape = CircleShape
            ),
        tint = tint
    )
}

@Composable
internal fun MobileIcon(
    tint: Color = Web3ModalTheme.colors.inverse100
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_mobile),
        contentDescription = ContentDescription.MOBILE.description,
        tint = tint,
        modifier = Modifier.size(12.dp),
    )
}

@Composable
internal fun WebIcon(
    tint: Color = Web3ModalTheme.colors.inverse100
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_web),
        contentDescription = ContentDescription.WEB.description,
        tint = tint,
        modifier = Modifier.size(12.dp),
    )
}

@Composable
internal fun ForwardIcon(
    tint: Color = Web3ModalTheme.colors.inverse100
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_chevron_right),
        contentDescription = ContentDescription.FORWARD_ARROW.description,
        tint = tint,
        modifier = Modifier.size(12.dp),
    )
}

@Composable
@UiModePreview
private fun IconsPreview() {
    MultipleComponentsPreview(
        { BackArrowIcon {} },
        { QuestionMarkIcon() },
        { CloseIcon {} },
        { RetryIcon() },
        { DeclinedIcon() },
        { WalletIcon() },
        { ExternalIcon() },
        { ScanQRIcon {} },
        { CopyIcon() },
        { RecentTransactionIcon() },
        { DisconnectIcon() },
        { AllWalletsIcon() },
        { SelectNetworkIcon() },
        { MobileIcon() },
        { WebIcon() }
    )
}
