package com.walletconnect.android.internal.common.storage.events

import android.database.sqlite.SQLiteException
import com.walletconnect.android.pulse.model.Event
import com.walletconnect.android.pulse.model.properties.Props
import com.walletconnect.android.sdk.storage.data.dao.EventDao
import com.walletconnect.android.sdk.storage.data.dao.EventQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventsRepository(
    private val eventQueries: EventQueries,
    private val bundleId: String,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    @Throws(SQLiteException::class)
    suspend fun insertOrAbort(props: Props.Error) = withContext(dispatcher) {
        with(Event(bundleId = bundleId, props = props)) {
            eventQueries.insertOrAbort(
                eventId,
                bundleId,
                timestamp,
                props.event,
                props.type,
                props.properties?.topic,
                props.properties?.trace
            )
        }
    }

    @Throws(SQLiteException::class)
    suspend fun getAll(): List<EventDao> {
        return withContext(dispatcher) {
            eventQueries.getAll().executeAsList()
        }
    }

    @Throws(SQLiteException::class)
    suspend fun deleteAll() {
        return withContext(dispatcher) {
            eventQueries.deleteAll()
        }
    }

    @Throws(SQLiteException::class)
    suspend fun deleteById(eventId: Long) {
        return withContext(dispatcher) {
            eventQueries.deleteById(eventId)
        }
    }
}