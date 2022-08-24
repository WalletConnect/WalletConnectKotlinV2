package com.walletconnect.requester.ui.connect.pairing_generate

import android.content.*
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.snackbar.Snackbar
import com.walletconnect.requester.R
import com.walletconnect.requester.databinding.DialogConnectUriBinding
import com.walletconnect.requester.ui.connect.ConnectViewModel
import com.walletconnect.sample_common.viewBinding
import net.glxn.qrgen.android.QRCode

class PairingGenerationDialogFragment : DialogFragment(R.layout.dialog_connect_uri) {
    private val viewModel: ConnectViewModel by navGraphViewModels(R.id.connectGraph)
    private val binding by viewBinding(DialogConnectUriBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        viewModel.connectToWallet { uri ->
            val deeplinkPairingUri = uri.replace("wc:", "wc:/")
            val qr = QRCode.from(uri).bitmap()

            binding.ivUri.setImageBitmap(qr)
            binding.btnCopyToClipboard.setOnClickListener {
                val clipBoard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("WalletConnect Pairing URI", uri)
                clipBoard.setPrimaryClip(clipData)

                Snackbar.make(binding.root, "Copied to Clipboard", Snackbar.LENGTH_SHORT).show()
            }

            binding.btnDeepLink.setOnClickListener {
                try {
                    requireActivity().startActivity(Intent(Intent.ACTION_VIEW, deeplinkPairingUri.toUri()))
                } catch (exception: ActivityNotFoundException) {
                    // There is no app to handle deep link
                }
            }
        }
    }
}