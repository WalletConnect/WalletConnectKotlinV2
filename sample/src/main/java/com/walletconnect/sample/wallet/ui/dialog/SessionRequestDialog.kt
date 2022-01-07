package com.walletconnect.sample.wallet.ui.dialog

import android.content.Context
import android.net.Uri
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.walletconnect.sample.databinding.SessionRequestDialogBinding
import com.walletconnect.walletconnectv2.client.model.WalletConnectClientData

class SessionRequestDialog(
    context: Context,
    val approve: (sessionRequest: WalletConnectClientData.SessionRequest) -> Unit,
    val reject: (sessionRequest: WalletConnectClientData.SessionRequest) -> Unit,
    private val sessionRequest: WalletConnectClientData.SessionRequest,
    private val session: WalletConnectClientData.SettledSession
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