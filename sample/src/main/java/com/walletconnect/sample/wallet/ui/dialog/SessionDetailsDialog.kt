package com.walletconnect.sample.wallet.ui.dialog

import android.content.Context
import android.net.Uri
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.walletconnect.sample.databinding.SessionDetailsDialogBinding
import com.walletconnect.walletconnectv2.client.WalletConnect

class SessionDetailsDialog(
    context: Context,
    private val session: WalletConnect.Model.Session,
    getPendingRequests: (session: WalletConnect.Model.Session) -> Unit
) : BottomSheetDialog(context) {

    private val binding = SessionDetailsDialogBinding.inflate(layoutInflater)

    init {
        setContentView(binding.root)
        setContent()
        getPendingRequests(session)
    }

    private fun setContent() = with(binding) {
        Glide.with(context)
            .load(Uri.parse(session.metaData?.icons?.first().toString()))
            .into(icon)
        name.text = session.metaData?.name
        uri.text = session.metaData?.url
        description.text = session.metaData?.description

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