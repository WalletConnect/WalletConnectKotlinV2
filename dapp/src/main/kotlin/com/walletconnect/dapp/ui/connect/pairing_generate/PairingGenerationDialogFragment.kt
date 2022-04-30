package com.walletconnect.dapp.ui.connect.pairing_generate

import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.snackbar.Snackbar
import com.walletconnect.dapp.R
import com.walletconnect.dapp.databinding.DialogConnectUriBinding
import com.walletconnect.dapp.ui.connect.ConnectViewModel
import com.walletconnect.sample_common.tag
import com.walletconnect.walletconnectv2.client.WalletConnect
import net.glxn.qrgen.android.QRCode

class PairingGenerationDialogFragment : DialogFragment(R.layout.dialog_connect_uri) {
    private val viewModel: ConnectViewModel by navGraphViewModels(R.id.connectGraph)
    private var _binding: DialogConnectUriBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val binding = DialogConnectUriBinding.bind(view).also { _binding = it }
        viewModel.connectToWallet { proposedSequence ->
            if (proposedSequence is WalletConnect.Model.ProposedSequence.Pairing) {
                val pairingUri = proposedSequence.uri.also {
                    Log.e(tag(this@PairingGenerationDialogFragment), it)
                }
                val deeplinkPairingUri = pairingUri.replace("wc:", "wc:/")
                val qr = QRCode.from(pairingUri).bitmap()

                binding.ivUri.setImageBitmap(qr)
                binding.btnCopyToClipboard.setOnClickListener {
                    val clipBoard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText("WalletConnect Pairing URI", pairingUri)
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
            } else {
                findNavController().popBackStack(R.id.fragment_chain_selection, true)
            }
        }
    }
}