package org.walletconnect.example.wallet.ui

import android.content.Context
import android.net.Uri
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.walletconnect.example.databinding.SessionProposalDialogBinding
import org.walletconnect.walletconnectv2.client.SessionProposal

class SessionProposalDialog(
    context: Context,
    val approve: () -> Unit,
    val reject: () -> Unit,
    private val proposal: SessionProposal
) : BottomSheetDialog(context) {

    private val binding = SessionProposalDialogBinding.inflate(layoutInflater)

    init {
        setContentView(binding.root)
        setContent()
    }

    private fun setContent() = with(binding) {
        Glide.with(context)
            .load(Uri.parse(proposal.icons.first().toString()))
            .into(icon)
        name.text = proposal.name
        uri.text = proposal.url
        description.text = proposal.description
        var chainsString = ""
        proposal.chains.forEach {
            chainsString += "$it\n"
        }
        chains.text = chainsString

        var methodsString = ""
        proposal.methods.forEach {
            methodsString += "$it\n"
        }
        methods.text = methodsString

        approve.setOnClickListener {
            approve()
        }

        reject.setOnClickListener {
            reject()
        }
    }
}