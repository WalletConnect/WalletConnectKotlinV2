package com.walletconnect.android.internal.utils

class ObservableMap<K, V>(
    private val map: MutableMap<K, V> = mutableMapOf(),
    private val onChange: (Map<K, V>) -> Unit
) : MutableMap<K, V> by map {

    override fun put(key: K, value: V): V? {
        return map.put(key, value).also { onChange(map) }
    }

    override fun remove(key: K): V? {
        return map.remove(key).also { onChange(map) }
    }

    override fun putAll(from: Map<out K, V>) {
        return map.putAll(from).also { onChange(map) }
    }
}