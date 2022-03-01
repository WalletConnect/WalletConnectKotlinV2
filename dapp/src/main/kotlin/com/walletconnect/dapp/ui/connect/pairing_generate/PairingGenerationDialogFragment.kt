package com.walletconnect.dapp.ui.connect.pairing_generate

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.navGraphViewModels
import com.google.android.material.snackbar.Snackbar
import com.walletconnect.dapp.R
import com.walletconnect.dapp.databinding.DialogConnectUriBinding
import com.walletconnect.dapp.ui.NavigationEvents
import com.walletconnect.dapp.ui.connect.ConnectViewModel
import net.glxn.qrgen.android.QRCode

class PairingGenerationDialogFragment : DialogFragment() {
    private val viewModel: ConnectViewModel by navGraphViewModels(R.id.connectGraph)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel.navigation.observe(this) {
            if (it is NavigationEvents.SessionApproved || it is NavigationEvents.SessionRejected) {
                dismiss()
            }
        }

        val binding = DialogConnectUriBinding.inflate(LayoutInflater.from(requireContext())).apply {
            val pairingUri: String? = viewModel.connectToWallet()
            val qr = QRCode.from(pairingUri).bitmap()

            this.ivUri.setImageBitmap(qr)
            this.btnCopyToClipboard.setOnClickListener {
                val clipBoard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("WalletConnect Pairing URI", pairingUri)
                clipBoard.setPrimaryClip(clipData)

                Snackbar.make(this.root, "Copied to Clipboard", Snackbar.LENGTH_SHORT).show()
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .show()
    }
}