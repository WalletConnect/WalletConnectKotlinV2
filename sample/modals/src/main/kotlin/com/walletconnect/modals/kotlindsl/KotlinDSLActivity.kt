package com.walletconnect.modals.kotlindsl

import android.os.Bundle
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.createGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.fragment
import androidx.navigation.ui.AppBarConfiguration
import com.walletconnect.modals.R
import com.walletconnect.modals.common.Route
import com.walletconnect.web3.modal.ui.web3Modal

class KotlinDSLActivity : AppCompatActivity(R.layout.activity_kotlin_dsl) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.graph = navController.createGraph(
            startDestination = Route.Home.path
        ) {
            fragment<HomeFragment>(Route.Home.path)
            web3Modal()
        }
    }
}