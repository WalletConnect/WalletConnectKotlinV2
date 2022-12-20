@file:JvmSynthetic

package com.walletconnect.push.common.di

import android.media.CamcorderProfile.getAll
import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.model.ClientParams
import com.walletconnect.push.common.adapters.JsonRpcResultAdapter
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.reflect.jvm.jvmName
import com.walletconnect.android.internal.common.di.commonModule as androidCommonModule

@JvmSynthetic
internal fun pushCommonModule() = module {

//    includes(androidCommonModule())

//    single {
//        get<Moshi.Builder>(named(AndroidCommonDITags.MOSHI))
//            .add { type, _, moshi ->
//                when (type.getRawType().name) {
//                    JsonRpcResponse.JsonRpcResult::class.jvmName -> JsonRpcResultAdapter(moshi)
//                    else -> null
//                }
//            }
//    }
}