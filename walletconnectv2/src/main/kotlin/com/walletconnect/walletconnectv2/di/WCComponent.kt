package com.walletconnect.walletconnectv2.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = [WCModule::class])
internal interface WCComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun useTls(useTls: Boolean): Builder

        @BindsInstance
        fun hostName(@Named("hostName") hostName: String): Builder

        @BindsInstance
        fun projectID(@Named("projectID") projectID: String): Builder

        @BindsInstance
        fun application(application: Application): Builder

        fun create(): WCComponent
    }

    fun engineFactory(): EngineFactory
}