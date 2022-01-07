package com.walletconnect.sample.wallet.ui.dialog

import android.content.Context
import android.net.Uri
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.walletconnect.sample.databinding.SessionRequestDialogBinding
import com.walletconnect.walletconnectv2.client.model.WalletConnectClientModel

class SessionRequestDialog(
    context: Context,
    val approve: (sessionRequest: WalletConnectClientModel.SessionRequest) -> Unit,
    val reject: (sessionRequest: WalletConnectClientModel.SessionRequest) -> Unit,
    private val sessionRequest: WalletConnectClientModel.SessionRequest,
    private val session: WalletConnectClientModel.SettledSession
) : BottomSheetDialog(context) {

    private val binding = SessionRequestDialogBinding.inflate(layoutInflater)

    init {
        setCancelable(false)
        setContentView(binding.root)
        setContent()
    }

    private fun setContent() = with(binding) {
        Glide.with(context)
            .load(Uri.parse(session.peerAppMetaData?.icons?.first().toString()))
            .into(icon)
        name.text = session.peerAppMetaData?.name
        uri.text = session.peerAppMetaData?.url
        message.setTitleAndBody("Params", sessionRequest.request.params)
        chains.text = sessionRequest.chainId
        methods.text = sessionRequest.request.method

        approve.setOnClickListener {
            dismiss()
            approve(sessionRequest)
        }

        reject.setOnClickListener {
            dismiss()
            reject(sessionRequest)
        }
    }
}