package com.walletconnect.modals.kotlindsl

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.walletconnect.modals.R
import com.walletconnect.modals.databinding.FragmentHomeBinding
import com.walletconnect.sample.common.viewBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val binding by viewBinding(FragmentHomeBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}