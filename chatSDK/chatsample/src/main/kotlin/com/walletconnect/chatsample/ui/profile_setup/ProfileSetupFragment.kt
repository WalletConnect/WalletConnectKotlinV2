package com.walletconnect.chatsample.ui.profile_setup

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.walletconnect.chatsample.R
import com.walletconnect.chatsample.databinding.FragmentProfileSetupBinding
import com.walletconnect.chatsample.utils.viewBinding

class ProfileSetupFragment: Fragment(R.layout.fragment_profile_setup) {
    private val binding by viewBinding(FragmentProfileSetupBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tbProfileSetup.setupWithNavController(findNavController())
        binding.tbProfileSetup.title = null

        binding.mbHomeCta.setOnClickListener {
            findNavController().navigate(R.id.action_profileSetupFragment_to_threadsFragment)
        }
    }
}