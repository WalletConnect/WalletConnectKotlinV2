@file:Suppress("PackageDirectoryMismatch")

package com.walletconnect.utils

import com.walletconnect.android.impl.di.AndroidCoreDITags
import com.walletconnect.android.impl.utils.CURRENT_TIME_IN_SECONDS
import com.walletconnect.android.impl.utils.Logger
import com.walletconnect.android.internal.common.SerializableJsonRpc
import com.walletconnect.android.internal.common.model.Expiry
import org.koin.core.module.KoinDefinition
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.ext.getFullName
import kotlin.reflect.KClass

@get:JvmSynthetic
val String.Companion.Empty
    get() = ""

@get:JvmSynthetic
val Int.Companion.DefaultId
    get() = -1

@JvmSynthetic
fun Long.extractTimestamp() = this / 1000

@JvmSynthetic
fun Expiry.isSequenceValid(): Boolean = seconds > CURRENT_TIME_IN_SECONDS

@get:JvmSynthetic
val String.Companion.HexPrefix
    get() = "0x"

inline fun <reified T : SerializableJsonRpc> Module.intoMultibindingSet(value: KClass<T>): KoinDefinition<*> =
    single(
        qualifier = named("key_${T::class.getFullName()}"),
        createdAtStart = true
    ) {
        Logger.log("intoMultibindingSet ${value::class.getFullName()}")
        val multiSet: MutableSet<KClass<T>> = get(named(AndroidCoreDITags.SERIALIZER_SET))
        multiSet.add(value)
        value
    }


fun Module.intoMultibindingMap(key: String, value: KClass<*>): KoinDefinition<*> =
    single(
        qualifier = named("${key::class.getFullName()}_${value::class.getFullName()}_$key"),
        createdAtStart = true
    ) {
        val multibindingMap: MutableMap<String, KClass<*>> = get(named(AndroidCoreDITags.DESERIALIZER_MAP))
        multibindingMap[key] == value
        key to value
    }