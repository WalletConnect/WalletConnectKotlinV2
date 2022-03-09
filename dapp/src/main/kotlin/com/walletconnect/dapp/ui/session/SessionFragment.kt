package com.walletconnect.dapp.ui.session

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.walletconnect.dapp.R
import com.walletconnect.dapp.databinding.FragmentSessionBinding
import com.walletconnect.dapp.ui.BottomVerticalSpaceItemDecoration
import com.walletconnect.dapp.ui.NavigationEvents
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SessionFragment : Fragment() {
    private val viewModel: SessionViewModel by viewModels()
    private var _binding: FragmentSessionBinding? = null
    private val binding: FragmentSessionBinding
        get() = _binding!!
    private val sessionAccountAdapter by lazy {
        SessionAdapter() { selectedAccount ->
            val selectedAccountKey = getString(R.string.selected_account)
            findNavController().navigate(R.id.action_fragment_session_to_fragment_selected_account, bundleOf(selectedAccountKey to selectedAccount))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSessionBinding.inflate(inflater, container, false).also { _binding = it }
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigation
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.RESUMED)
            .onEach { events ->
                when (events) {
                    is NavigationEvents.PingSuccess -> Toast.makeText(requireContext(), "Pinged Peer Successfully on Topic: ${events.topic}", Toast.LENGTH_SHORT).show()
                    is NavigationEvents.PingError -> Toast.makeText(requireContext(), "Pinged Peer Unsuccessfully", Toast.LENGTH_SHORT).show()
                    is NavigationEvents.Disconnect -> findNavController().popBackStack()
                    is NavigationEvents.UpdatedListOfAccounts -> sessionAccountAdapter.submitList(events.listOfAccounts)
                    else -> Unit
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        with(binding.rvAccounts) {
            adapter = (sessionAccountAdapter).apply {
                submitList(viewModel.getListOfAccounts())
            }
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.disconnect()
    }
}