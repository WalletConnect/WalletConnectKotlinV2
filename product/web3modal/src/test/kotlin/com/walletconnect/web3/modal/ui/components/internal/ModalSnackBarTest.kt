package com.walletconnect.web3.modal.ui.components.internal

import com.android.resources.NightMode
import com.walletconnect.web3.modal.ui.components.internal.snackbar.ModalSnackBar
import com.walletconnect.web3.modal.ui.components.internal.snackbar.SnackBarEvent
import com.walletconnect.web3.modal.ui.components.internal.snackbar.SnackBarEventType
import com.walletconnect.web3.modal.ui.components.internal.snackbar.SnackbarDuration
import com.walletconnect.web3.modal.utils.ScreenShotTest
import org.junit.Test

internal class ModalSnackBarTest : ScreenShotTest("component/internal/snackbar") {

    private class Event(
        override val type: SnackBarEventType,
        override val message: String,
        override val duration: SnackbarDuration
    ) : SnackBarEvent {
        override fun dismiss() {}
    }

    @Test
    fun `test ModalSnackBar in LightMode`() = runComponentScreenShotTest {
        val events = listOf(
            Event(SnackBarEventType.SUCCESS, "Address copied", SnackbarDuration.SHORT),
            Event(SnackBarEventType.INFO, "Signature canceled", SnackbarDuration.SHORT),
            Event(SnackBarEventType.ERROR, "Network error", SnackbarDuration.SHORT),
        )
        events.forEach {
            ModalSnackBar(snackBarEvent = it)
        }
    }

    @Test
    fun `test ModalSnackBar in DarkMode`() = runComponentScreenShotTest(
        nightMode = NightMode.NIGHT
    ) {
        val events = listOf(
            Event(SnackBarEventType.SUCCESS, "Address copied", SnackbarDuration.SHORT),
            Event(SnackBarEventType.INFO, "Signature canceled", SnackbarDuration.SHORT),
            Event(SnackBarEventType.ERROR, "Network error", SnackbarDuration.SHORT),
        )
        events.forEach {
            ModalSnackBar(snackBarEvent = it)
        }
    }

}