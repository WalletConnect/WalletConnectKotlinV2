package com.walletconnect.android.internal.common.storage.events

import android.database.sqlite.SQLiteException
import app.cash.sqldelight.async.coroutines.awaitAsList
import com.walletconnect.android.internal.common.model.TelemetryEnabled
import com.walletconnect.android.pulse.model.Event
import com.walletconnect.android.pulse.model.properties.Properties
import com.walletconnect.android.pulse.model.properties.Props
import com.walletconnect.android.sdk.storage.data.dao.EventDao
import com.walletconnect.android.sdk.storage.data.dao.EventQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventsRepository(
    private val eventQueries: EventQueries,
    private val bundleId: String,
    private val telemetryEnabled: TelemetryEnabled,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    @Throws(SQLiteException::class)
    suspend fun insertOrAbortTelemetry(props: Props) = withContext(dispatcher) {
        if (telemetryEnabled.value) {
            insertOrAbort(props)
        }
    }

    @Throws(SQLiteException::class)
    suspend fun insertOrAbort(props: Props) = withContext(dispatcher) {
        with(Event(bundleId = bundleId, props = props)) {
            eventQueries.insertOrAbort(
                eventId,
                bundleId,
                timestamp,
                this.props.event,
                this.props.type,
                this.props.properties?.topic,
                this.props.properties?.trace,
                this.props.properties?.correlationId,
                this.props.properties?.clientId,
                this.props.properties?.direction,
                this.props.properties?.userAgent
            )
        }
    }

    @Throws(SQLiteException::class)
    suspend fun getAllEventsWithLimitAndOffset(limit: Int, offset: Int): List<Event> {
        return eventQueries.getAllEventsWithLimitAndOffset(limit.toLong(), offset.toLong())
            .awaitAsList()
            .map { dao -> dao.toEvent() }
    }

    @Throws(SQLiteException::class)
    suspend fun getAllNonTelemetryEventsWithLimitAndOffset(limit: Int, offset: Int): List<Event> {
        return eventQueries.getAllEventsWithLimitAndOffset(limit.toLong(), offset.toLong())
            .awaitAsList()
            .filter { dao -> dao.correlation_id != null }
            .map { dao -> dao.toEvent() }
    }

    @Throws(SQLiteException::class)
    suspend fun deleteAllTelemetry() {
        return withContext(dispatcher) {
            eventQueries.deleteAllTelemetry()
        }
    }

    @Throws(SQLiteException::class)
    suspend fun deleteByIds(eventIds: List<Long>) {
        withContext(dispatcher) {
            eventQueries.deleteByIds(eventIds)
        }
    }

    private fun EventDao.toEvent(): Event =
        Event(
            eventId = event_id,
            bundleId = bundle_id,
            timestamp = timestamp,
            props = Props(
                event = event_name,
                type = type,
                properties = Properties(
                    topic = topic,
                    trace = trace,
                    clientId = client_id,
                    correlationId = correlation_id,
                    direction = direction,
                    userAgent = user_agent
                )
            )
        )
}