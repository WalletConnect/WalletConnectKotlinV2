package com.walletconnect.sample.modal.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.walletconnect.sample.modal.R
import com.walletconnect.web3.modal.ui.Web3ModalView

class ViewActivity: AppCompatActivity(R.layout.activity_view) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = findViewById<Web3ModalView>(R.id.web3Modal)

        view.setOnCloseModal { finish() }
    }

}