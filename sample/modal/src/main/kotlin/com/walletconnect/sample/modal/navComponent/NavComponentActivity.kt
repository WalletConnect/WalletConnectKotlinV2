package com.walletconnect.sample.modal.navComponent

import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.walletconnect.sample.modal.R

class NavComponentActivity: AppCompatActivity(R.layout.activity_nav_component) {

    private val navHostFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.fragment_host) as NavHostFragment
    }


    override fun onSupportNavigateUp(): Boolean {
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }
}