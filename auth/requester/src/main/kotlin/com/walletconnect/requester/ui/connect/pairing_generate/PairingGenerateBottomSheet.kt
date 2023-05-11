package com.walletconnect.requester.ui.connect.pairing_generate

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.github.alexzhirkevich.customqrgenerator.QrCodeGenerator
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.ThreadPolicy
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.walletconnect.requester.R
import com.walletconnect.requester.databinding.DialogConnectUriBinding
import com.walletconnect.requester.ui.connect.ConnectViewModel
import kotlinx.coroutines.launch

class PairingGenerateBottomSheet : BottomSheetDialogFragment() {
    private val viewModel: ConnectViewModel by navGraphViewModels(R.id.connectGraph)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return DialogConnectUriBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(DialogConnectUriBinding.bind(view)) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.connectToWallet { uri ->
            val deeplinkPairingUri = uri.replace("wc:", "wc://")
            val data = QrData.Url(uri)

            viewLifecycleOwner.lifecycleScope.launch {
                val threadPolicy = when (Runtime.getRuntime().availableProcessors()) {
                    in 1..3 -> ThreadPolicy.SingleThread
                    in 4..6 -> ThreadPolicy.DoubleThread
                    else -> ThreadPolicy.QuadThread
                }

                val generator = QrCodeGenerator(requireContext(), threadPolicy)

                val qr = generator.generateQrCode(data, qrOptions)
                ivUri.setImageBitmap(qr)
            }

            btnCopyToClipboard.setOnClickListener {
                val clipBoard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("WalletConnect Pairing URI", uri)
                clipBoard.setPrimaryClip(clipData)

                Snackbar.make(root.rootView, "Copied to Clipboard", Snackbar.LENGTH_SHORT).show()
            }

            btnDeepLink.setOnClickListener {
                try {

                    println("kobe: Requester deep link: ${deeplinkPairingUri.toUri()}")

                    requireActivity().startActivity(Intent(Intent.ACTION_VIEW, deeplinkPairingUri.toUri()))
                } catch (exception: ActivityNotFoundException) {
                    // There is no app to handle deep link
                }
            }

            tvCancel.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }
}