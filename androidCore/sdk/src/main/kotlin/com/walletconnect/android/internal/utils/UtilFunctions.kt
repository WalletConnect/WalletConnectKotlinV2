@file:Suppress("PackageDirectoryMismatch")

package com.walletconnect.utils

import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.type.SerializableJsonRpc
import com.walletconnect.android.internal.utils.CURRENT_TIME_IN_SECONDS
import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.ext.getFullName
import java.util.BitSet
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

fun <T : SerializableJsonRpc> Module.addSerializerEntry(value: KClass<T>): KoinDefinition<KClass<T>> =
    single(qualifier = named("key_${value.getFullName()}")) { value }

fun Module.addSdkBitsetForUA(bitSet: BitSet): KoinDefinition<BitSet> =
    single(qualifier = named(bitSet.toBinaryString())) { bitSet }

fun Module.addDeserializerEntry(key: String, value: KClass<*>): KoinDefinition<Pair<String, KClass<*>>> =
    single(qualifier = named("${key}_${value.getFullName()}")) { key to value }

/**
 * When converting a BitSet to a byte array, the BitSet is converted to a byte array with a size of 8 in little-endian order.<br>
 * This means there will be leading zeros in front of the binary string representation of the BitSet
 */
@JvmSynthetic
internal fun BitSet.toBinaryString(): String =
    this.toByteArray().joinToString { byte ->
        "%8s".format(Integer.toBinaryString(byte.toInt() and 0xFF)).replace(' ', '0')
    }

@JvmSynthetic
internal fun String.removeLeadingZeros(): String = this.replaceFirst("^0+(?!$)".toRegex(), "")