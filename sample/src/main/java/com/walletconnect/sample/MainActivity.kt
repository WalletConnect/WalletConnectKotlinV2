package com.walletconnect.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.walletconnect.sample.databinding.ActivityMainBinding
import org.walletconnect.walletconnectv2.WalletConnectClient

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setBottomNavigation()
    }

    override fun onDestroy() {
        super.onDestroy()

        WalletConnectClient.shutdown()
    }

    private fun setBottomNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment
        NavigationUI.setupWithNavController(binding.bottomNav, navHostFragment.findNavController())
        navHostFragment.findNavController().addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.isGone = destination.id == R.id.scannerFragment
        }
    }
} 