@file:Suppress("PackageDirectoryMismatch")

package com.walletconnect.utils

import com.walletconnect.android.internal.common.SerializableJsonRpc
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.impl.di.AndroidCoreDITags
import com.walletconnect.android.impl.utils.CURRENT_TIME_IN_SECONDS
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

fun Module.intoMultibindingSet(value: (SerializableJsonRpc) -> Boolean) {
    single(
        qualifier = named("key_${value::class.getFullName()}"),
        createdAtStart = true
    ) {
        val multiSet = get<MutableSet<(SerializableJsonRpc) -> Boolean>>(named(AndroidCoreDITags.SERIALIZER_SET))
        multiSet.add(value)
    }
}

fun Module.intoMultibindingMap(key: String, value: KClass<*>) {
    single(
        qualifier = named("${key::class.getFullName()}_${value::class.getFullName()}_$key"),
        createdAtStart = true
    ) {
        val multibindingMap = get<MutableMap<String, KClass<*>>>(named(AndroidCoreDITags.DESERIALIZER_MAP))
        multibindingMap[key] == value
    }
}