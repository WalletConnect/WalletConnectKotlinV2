package com.walletconnect.wallet.ui.accounts.uri

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.walletconnect.wallet.ACCOUNTS_ARGUMENT_KEY
import com.walletconnect.wallet.databinding.BottomsheetUriBinding

class UriBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: BottomsheetUriBinding? = null
    private val binding: BottomsheetUriBinding
        get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return BottomsheetUriBinding.inflate(inflater, container, false).also {
            _binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnOk.setOnClickListener {
            findNavController().previousBackStackEntry?.savedStateHandle?.set(ACCOUNTS_ARGUMENT_KEY, binding.tvPasteUri.text.toString())
            findNavController().popBackStack()
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}