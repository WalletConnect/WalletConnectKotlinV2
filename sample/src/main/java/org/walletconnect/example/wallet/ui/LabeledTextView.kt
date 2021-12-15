package org.walletconnect.example.wallet.ui

import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import org.walletconnect.example.R
import org.walletconnect.example.databinding.LabeledTextViewBinding

class LabeledTextView(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    private var binding = LabeledTextViewBinding.bind(inflate(context, R.layout.labeled_text_view, this))

    init {
        binding.body.movementMethod = ScrollingMovementMethod()
    }

    fun setTitleAndBody(titleText: String, bodyText: String) {
        binding.apply {
            title.text = titleText
            body.text = bodyText
        }
    }
}