@file:Suppress("PackageDirectoryMismatch")

package com.walletconnect.utils

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.type.SerializableJsonRpc
import com.walletconnect.android.internal.utils.CURRENT_TIME_IN_SECONDS
import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.ext.getFullName
import java.net.URI
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

fun Module.addDeserializerEntry(key: String, value: KClass<*>): KoinDefinition<Pair<String, KClass<*>>> =
    single(qualifier = named("${key}_${value.getFullName()}")) { key to value }

// Had to add JsonAdapterEntry because Koin would fetch the wrong values when using Pair instead
data class JsonAdapterEntry<T>(val type: Class<T>, val adapter: (Moshi) -> JsonAdapter<T>)

fun <T> Module.addJsonAdapter(type: Class<T>, adapter: (Moshi) -> JsonAdapter<T>): KoinDefinition<JsonAdapterEntry<T>> {
    val jsonAdapterEntry = JsonAdapterEntry(type, adapter)
    return single(qualifier = named("$jsonAdapterEntry")) { jsonAdapterEntry }
}

/**
 * When converting a BitSet to a byte array, the BitSet is converted to a byte array with a dynamic size that is divisible by 8 in little-endian (right to left) order.
 * This means there will be leading zeros in front of the binary string representation of the BitSet
 */
@JvmSynthetic
internal fun BitSet.toBinaryString(): String =
    this.toByteArray().joinToString { byte ->
        "%8s".format(Integer.toBinaryString(byte.toInt() and 0xFF)).replace(' ', '0')
    }.replace(", ", "")

@JvmSynthetic
internal fun String.removeLeadingZeros(): String = this.replaceFirst("^0+(?!$)".toRegex(), "")

@JvmSynthetic
internal fun combineListOfBitSetsWithOrOperator(bitSets: List<BitSet>): BitSet {
    return bitSets.reduce { acc, bitSet ->
        acc.or(bitSet)
        acc
    }
}

@JvmSynthetic
internal fun compareDomains(metadataUrl: String, originUrl: String): Boolean {
    val metadataDomain = URI(metadataUrl).host.removePrefix("www.")
    val originDomain = URI(originUrl).host.removePrefix("www.")
    return metadataDomain == originDomain
}