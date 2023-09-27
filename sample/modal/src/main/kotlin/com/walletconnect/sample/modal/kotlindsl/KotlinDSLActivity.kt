package com.walletconnect.sample.modal.kotlindsl

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.createGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.fragment
import com.walletconnect.sample.modal.common.Route
import com.walletconnect.sample.modal.R
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