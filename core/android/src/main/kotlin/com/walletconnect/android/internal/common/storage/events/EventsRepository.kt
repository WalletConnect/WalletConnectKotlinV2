package com.walletconnect.android.internal.common.storage.events

import android.database.sqlite.SQLiteException
import com.walletconnect.android.pulse.model.Event
import com.walletconnect.android.pulse.model.properties.Props
import com.walletconnect.android.sdk.storage.data.dao.EventDao
import com.walletconnect.android.sdk.storage.data.dao.EventQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventsRepository(private val eventQueries: EventQueries, private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {

    @Throws(SQLiteException::class)
    suspend fun insertOrAbort(event: Event<Props.TraceProps>) = withContext(dispatcher) {
        eventQueries.insertOrAbort(event.eventId, event.bundleId, event.timestamp, event.props.event, event.props.type, event.props.properties?.topic!!, event.props.properties?.trace!!)
    }

    @Throws(SQLiteException::class)
    suspend fun getEvents(): List<EventDao> {
        return withContext(dispatcher) {
            eventQueries.getAll().executeAsList()
        }
    }
}