package org.walletconnect.example.wallet.ui

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.walletconnect.example.databinding.UrlDialogBinding

class UrlDialog(context: Context, val approve: (url: String) -> Unit) : BottomSheetDialog(context) {

    private val binding = UrlDialogBinding.inflate(layoutInflater)

    init {
        setContentView(binding.root)
        binding.ok.setOnClickListener {
            approve(binding.pasteUri.text.toString())
            dismiss()
        }
    }
}