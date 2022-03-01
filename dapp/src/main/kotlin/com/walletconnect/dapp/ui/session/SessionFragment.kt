package com.walletconnect.dapp.ui.session

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.walletconnect.dapp.R
import com.walletconnect.dapp.databinding.FragmentSessionBinding
import com.walletconnect.dapp.ui.BottomVerticalSpaceItemDecoration
import com.walletconnect.dapp.ui.NavigationEvents

class SessionFragment : Fragment() {
    private val viewModel: SessionViewModel by viewModels()
    private var _binding: FragmentSessionBinding? = null
    private val binding: FragmentSessionBinding
        get() = _binding!!
    private val sessionAccountAdapter by lazy {
        SessionAdapter() { selectedAccount ->
            val selectedAccountKey = getString(R.string.selected_account)
            this.findNavController().navigate(R.id.action_fragment_session_to_fragment_selected_account, bundleOf(selectedAccountKey to selectedAccount))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSessionBinding.inflate(inflater, container, false).also { _binding = it }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigation.observe(viewLifecycleOwner) { navigationEvent ->
            when (navigationEvent) {
                is NavigationEvents.PingSuccess -> Toast.makeText(requireContext(), "Pinged Peer Successfully on Topic: ${navigationEvent.topic}", Toast.LENGTH_SHORT).show()
                is NavigationEvents.PingError -> Toast.makeText(requireContext(), "Pinged Peer Unsuccessfully", Toast.LENGTH_SHORT).show()
                is NavigationEvents.Disconnect -> findNavController().navigate(R.id.action_fragment_session_to_connect_graph)
                is NavigationEvents.UpdatedListOfAccounts -> sessionAccountAdapter.submitList(navigationEvent.listOfAccounts)
                else -> Unit
            }
        }

        with(binding.rvAccounts) {
            adapter = sessionAccountAdapter.apply {
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    override fun onDestroy() {
        viewModel.disconnect()
        super.onDestroy()
    }
}