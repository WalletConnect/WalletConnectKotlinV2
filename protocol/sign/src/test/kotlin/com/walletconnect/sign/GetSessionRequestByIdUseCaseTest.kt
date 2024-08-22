package com.walletconnect.sign

import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.json_rpc.model.JsonRpcHistoryRecord
import com.walletconnect.android.internal.common.storage.rpc.JsonRpcHistory
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.common.model.vo.clientsync.session.payload.SessionRequestVO
import com.walletconnect.sign.json_rpc.domain.GetSessionRequestByIdUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetSessionRequestByIdUseCaseTest {
    private lateinit var jsonRpcHistory: JsonRpcHistory
    private lateinit var serializer: JsonRpcSerializer
    private lateinit var getSessionRequestByIdUseCase: GetSessionRequestByIdUseCase

    @Before
    fun setUp() {
        jsonRpcHistory = mockk()
        serializer = mockk()
        getSessionRequestByIdUseCase = GetSessionRequestByIdUseCase(jsonRpcHistory, serializer)
    }

    @Test
    fun `invoke should return null when record is null`() {
        val id = 123L

        every { jsonRpcHistory.getRecordById(id) } returns null

        val result = getSessionRequestByIdUseCase.invoke(id)

        assertNull(result)
        verify(exactly = 1) { jsonRpcHistory.getRecordById(id) }
    }

    @Test
    fun `invoke should return null when deserialization fails`() {
        val id = 123L
        val record = mockk<JsonRpcHistoryRecord>()
        val body = "testBody"

        every { jsonRpcHistory.getRecordById(id) } returns record
        every { record.body } returns body
        every { serializer.tryDeserialize<SignRpc.SessionRequest>(body) } returns null

        val result = getSessionRequestByIdUseCase.invoke(id)

        assertNull(result)
        verify(exactly = 1) { jsonRpcHistory.getRecordById(id) }
        verify(exactly = 1) { serializer.tryDeserialize<SignRpc.SessionRequest>(body) }
    }

    @Test
    fun `invoke should return request when deserialization succeeds`() {
        val id = 123L
        val record = mockk<JsonRpcHistoryRecord>(relaxed = true)
        val params = SignParams.SessionRequestParams(
            chainId = "1",
            request = SessionRequestVO(
                method = "method",
                params = "params",
                expiryTimestamp = null
            ),
        )
        val sessionRequest = SignRpc.SessionRequest(
            method = "method",
            params = params,
        )

        every { jsonRpcHistory.getRecordById(id) } returns record
        every { serializer.tryDeserialize<SignRpc.SessionRequest>(any()) } returns sessionRequest
        getSessionRequestByIdUseCase.invoke(id)

        verify(exactly = 1) { jsonRpcHistory.getRecordById(id) }
        verify(exactly = 1) { serializer.tryDeserialize<SignRpc.SessionRequest>(any()) }
    }
}