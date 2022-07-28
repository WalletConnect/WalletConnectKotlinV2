package com.walletconnect.chatsample.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.walletconnect.chatsample.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment: Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            delay(1500)
            findNavController().navigate(R.id.action_homeFragment_to_profileSetupFragment)
        }
    }
}