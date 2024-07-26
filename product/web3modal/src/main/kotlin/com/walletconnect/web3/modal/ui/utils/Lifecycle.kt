package com.walletconnect.web3.modal.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.walletconnect.web3.modal.ui.components.ComponentDelegate

@Composable
internal fun ComposableLifecycleEffect(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onEvent: (LifecycleOwner, Lifecycle.Event) -> Unit
) {
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { source, event ->
            onEvent(source, event)
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

internal fun Lifecycle.Event.toComponentEvent(onClosed: () -> Unit) {
    when (this) {
        Lifecycle.Event.ON_CREATE, Lifecycle.Event.ON_START, Lifecycle.Event.ON_RESUME -> ComponentDelegate.openModalEvent()

        Lifecycle.Event.ON_DESTROY, Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_PAUSE -> {
            onClosed()
            ComponentDelegate.closeModalEvent()
        }
        else -> Unit
    }
}
