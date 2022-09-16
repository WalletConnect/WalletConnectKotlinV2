package com.walletconnect.requester.ui.connect

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.snackbar.Snackbar
import com.walletconnect.requester.R
import com.walletconnect.requester.databinding.FragmentConnectBinding
import com.walletconnect.requester.ui.events.RequesterEvents
import com.walletconnect.sample_common.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ConnectFragment : Fragment(R.layout.fragment_connect) {
    private val binding by viewBinding(FragmentConnectBinding::bind)
    private val viewModel: ConnectViewModel by navGraphViewModels(R.id.connectGraph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rlSignIn.setOnClickListener {
            if (viewModel.anySettledPairingExist()) { //todo: Right now always false. Remove todo comment after implemented
                findNavController().navigate(R.id.action_fragment_chain_selection_to_dialog_pairing_selection)
            } else {
                findNavController().navigate(R.id.action_fragment_chain_selection_to_dialog_pairing_generation)
            }
        }

        viewModel.navigation
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.RESUMED)
            .onEach { events ->
                when (events) {
                    is RequesterEvents.OnAuthenticated -> findNavController().navigate(R.id.action_global_fragment_session)
                    is RequesterEvents.OnError -> {
                        findNavController().navigate(R.id.action_global_fragment_chain_selection)
                        Snackbar.make(binding.root, "Session was Rejected", Snackbar.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        binding.tvDescription.text = Html.fromHtml(getString(R.string.displayed_description), FROM_HTML_MODE_LEGACY)

        binding.tvViewDocs.setOnClickListener {
            try {
                requireActivity().startActivity(Intent(Intent.ACTION_VIEW, "https://docs.walletconnect.com/2.0/kotlin/auth/installation".toUri()))
            } catch (exception: ActivityNotFoundException) {
                // There is no app to handle deep link
            }
        }
    }

}