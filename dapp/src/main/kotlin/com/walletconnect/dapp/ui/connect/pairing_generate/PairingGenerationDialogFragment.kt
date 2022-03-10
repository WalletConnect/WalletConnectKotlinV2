package com.walletconnect.dapp.ui.connect.pairing_generate

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.snackbar.Snackbar
import com.walletconnect.dapp.R
import com.walletconnect.dapp.databinding.DialogConnectUriBinding
import com.walletconnect.dapp.tag
import com.walletconnect.dapp.ui.connect.ConnectViewModel
import com.walletconnect.walletconnectv2.client.WalletConnect
import net.glxn.qrgen.android.QRCode

class PairingGenerationDialogFragment : DialogFragment() {
    private val viewModel: ConnectViewModel by navGraphViewModels(R.id.connectGraph)
    private var _binding: DialogConnectUriBinding? = null
    private val binding: DialogConnectUriBinding
        get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogConnectUriBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val proposedSequence: WalletConnect.Model.ProposedSequence = viewModel.connectToWallet()

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

            //TODO: Uncomment once refactor merged in
//                try {
//                    requireActivity().startActivity(Intent(Intent.ACTION_VIEW, deeplinkPairingUri.toUri()))
//                } catch (exception: ActivityNotFoundException) {
//                    // There is no app to handle deep link
//                }
        } else {
            findNavController().popBackStack(R.id.fragment_chain_selection, true)
        }
    }
}