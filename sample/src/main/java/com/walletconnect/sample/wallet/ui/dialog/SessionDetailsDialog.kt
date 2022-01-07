package com.walletconnect.sample.wallet.ui.dialog

import android.content.Context
import android.net.Uri
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.walletconnect.sample.databinding.SessionDetailsDialogBinding
import com.walletconnect.walletconnectv2.client.model.WalletConnectClientData

class SessionDetailsDialog(context: Context, private val session: WalletConnectClientData.SettledSession) : BottomSheetDialog(context) {

    private val binding = SessionDetailsDialogBinding.inflate(layoutInflater)

    init {
        setContentView(binding.root)
        setContent()
    }

    private fun setContent() = with(binding) {
        Glide.with(context)
            .load(Uri.parse(session.peerAppMetaData?.icons?.first().toString()))
            .into(icon)
        name.text = session.peerAppMetaData?.name
        uri.text = session.peerAppMetaData?.url
        description.text = session.peerAppMetaData?.description

        var accountsString = ""
        session.accounts.forEach {
            accountsString += "$it\n"
        }
        accounts.text = accountsString

        var methodsString = ""
        session.permissions.jsonRpc.methods.forEach {
            methodsString += "$it\n"
        }
        methods.text = methodsString

        close.setOnClickListener {
            dismiss()
        }
    }
}