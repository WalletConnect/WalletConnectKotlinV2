package com.walletconnect.sample.modal.navComponent

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.walletconnect.sample.modal.R
import com.walletconnect.sample.modal.databinding.FragmentHomeBinding
import com.walletconnect.sample.common.viewBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val binding by viewBinding(FragmentHomeBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}