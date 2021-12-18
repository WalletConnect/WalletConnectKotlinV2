package com.walletconnect.sample.wallet.ui.dialog

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.walletconnect.sample.databinding.UrlDialogBinding

class UrlDialog(context: Context, val pair: (url: String) -> Unit) : BottomSheetDialog(context) {

    private val binding = UrlDialogBinding.inflate(layoutInflater)

    init {
        setContentView(binding.root)
        binding.ok.setOnClickListener {
            pair(binding.pasteUri.text.toString())
            dismiss()
        }
    }
}