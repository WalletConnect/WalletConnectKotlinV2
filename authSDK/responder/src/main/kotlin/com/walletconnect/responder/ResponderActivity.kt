package com.walletconnect.responder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.walletconnect.responder.databinding.ActivityResponderBinding
import com.walletconnect.sample_common.viewBinding

class ResponderActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivityResponderBinding::inflate)
    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.fcvHost) as NavHostFragment).navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupActionBarWithNavController(navController, AppBarConfiguration(setOf(R.id.fragment_accounts)))
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}