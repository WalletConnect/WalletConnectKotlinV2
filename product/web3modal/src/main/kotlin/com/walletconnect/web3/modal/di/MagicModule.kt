package com.walletconnect.web3.modal.di

import com.walletconnect.web3.modal.domain.magic.handler.MagicController
import org.koin.dsl.module

internal fun magicModule() = module {

//    single {
//        println("kobe: lifecycle")
//        androidApplication().registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
//            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
//                println("kobe1: onActivityCreated")
//            }
//
//            override fun onActivityStarted(p0: Activity) {
//                println("kobe1: onActivityStarted")
//            }
//
//            override fun onActivityResumed(p0: Activity) {
//                println("kobe1: onActivityResumed")
//            }
//
//            override fun onActivityPaused(p0: Activity) {
//                println("kobe1: onActivityPaused")
//            }
//
//            override fun onActivityStopped(p0: Activity) {
//                println("kobe1: onActivityStopped")
//            }
//
//            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
//                println("kobe1: onActivitySaveInstanceState")
//            }
//
//            override fun onActivityDestroyed(p0: Activity) {
//                println("kobe1: onActivityDestroyed")
//            }
//        })
//    }

//    single {
//        println("kobe: engine")
//        MagicEngine(
////            context = get(),
//            logger = get(),
//            appMetaData = get(),
//            projectId = get()
//        )
//    }

    single {
        println("kobe: controller")
        MagicController(
//            magicEngine = get()
//            context = androidApplication(),
            logger = get(),
            appMetaData = get(),
            projectId = get()
        )
    }
}