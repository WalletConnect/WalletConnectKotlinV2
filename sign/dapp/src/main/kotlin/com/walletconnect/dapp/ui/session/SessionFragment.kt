package com.walletconnect.dapp.ui.session

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.walletconnect.dapp.R
import com.walletconnect.dapp.databinding.FragmentSessionBinding
import com.walletconnect.dapp.ui.SampleDappEvents
import com.walletconnect.sample_common.BottomVerticalSpaceItemDecoration
import com.walletconnect.sample_common.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SessionFragment : Fragment(R.layout.fragment_session) {
    private val binding by viewBinding(FragmentSessionBinding::bind)
    private val viewModel: SessionViewModel by viewModels()
    private val sessionAccountAdapter by lazy {
        SessionAdapter { selectedAccount ->
            val selectedAccountKey = getString(R.string.selected_account)
            findNavController().navigate(R.id.action_fragment_session_to_fragment_selected_account, bundleOf(selectedAccountKey to selectedAccount))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        viewModel.uiState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { listOfSessions: List<SessionUI> ->
                sessionAccountAdapter.submitList(listOfSessions)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.navigationEvents
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { events ->
                when (events) {
                    is SampleDappEvents.PingSuccess -> Toast.makeText(requireContext(), "Pinged Peer Successfully on Topic: ${events.topic}", Toast.LENGTH_SHORT).show()
                    is SampleDappEvents.PingError -> Toast.makeText(requireContext(), "Pinged Peer Unsuccessfully", Toast.LENGTH_SHORT).show()
                    is SampleDappEvents.Disconnect -> findNavController().popBackStack()
                    else -> Unit
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        with(binding.rvAccounts) {
            adapter = (sessionAccountAdapter)
            addItemDecoration(BottomVerticalSpaceItemDecoration(16))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_session, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_ping -> {
                viewModel.ping()

                false
            }
            R.id.menu_disconnect -> {
                viewModel.disconnect()

                false
            }
            android.R.id.home -> {
                viewModel.disconnect()

                false
            }
            R.id.menu_push_request -> {
                viewModel.pushRequest()

                false
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.disconnect()
    }
}