package com.walletconnect.android.internal.common.storage.events

import android.database.sqlite.SQLiteException
import app.cash.sqldelight.async.coroutines.awaitAsList
import com.walletconnect.android.internal.common.model.TelemetryEnabled
import com.walletconnect.android.pulse.model.Event
import com.walletconnect.android.pulse.model.properties.Properties
import com.walletconnect.android.pulse.model.properties.Props
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
    suspend fun insertOrAbort(props: Props) = withContext(dispatcher) {
        if (telemetryEnabled.value) {
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
                    this.props.properties?.direction
                )
            }
        }
    }

    @Throws(SQLiteException::class)
    suspend fun getAllWithLimitAndOffset(limit: Int, offset: Int): List<Event> {
        return eventQueries.getAllWithLimitAndOffset(limit.toLong(), offset.toLong())
            .awaitAsList()
            .map {
                Event(
                    eventId = it.event_id,
                    bundleId = it.bundle_id,
                    timestamp = it.timestamp,
                    props = Props(
                        event = it.event_name,
                        type = it.type,
                        properties = Properties(
                            topic = it.topic,
                            trace = it.trace,
                            clientId = it.client_id,
                            correlationId = it.correlation_id,
                            direction = it.direction
                        )
                    )
                )
            }
    }

    @Throws(SQLiteException::class)
    suspend fun deleteAll() {
        return withContext(dispatcher) {
            eventQueries.deleteAll()
        }
    }

    @Throws(SQLiteException::class)
    suspend fun deleteByIds(eventIds: List<Long>) {
        withContext(dispatcher) {
            eventQueries.deleteByIds(eventIds)
        }
    }
}