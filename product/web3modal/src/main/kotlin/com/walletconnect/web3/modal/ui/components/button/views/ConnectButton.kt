package com.walletconnect.web3.modal.ui.components.button.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.findNavController
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.components.button.ConnectButton
import com.walletconnect.web3.modal.ui.components.button.rememberWeb3ModalState
import com.walletconnect.web3.modal.utils.toConnectButtonSize

class ConnectButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ConnectButton, 0, 0)
        val connectButtonSize = typedArray.getInteger(R.styleable.ConnectButton_connect_button_size, 0).toConnectButtonSize()
        typedArray.recycle()

        LayoutInflater.from(context)
            .inflate(R.layout.view_button, this, true)
            .findViewById<ComposeView>(R.id.root)
            .apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    val web3ModalState = rememberWeb3ModalState(navController = findNavController())
                    ConnectButton(state = web3ModalState, buttonSize = connectButtonSize)
                }
            }
    }
}
