package com.walletconnect.sign

import com.walletconnect.android.internal.common.storage.rpc.JsonRpcHistory
import com.walletconnect.android.internal.common.storage.verify.VerifyContextStorageRepository
import com.walletconnect.sign.json_rpc.domain.DeleteRequestByIdUseCase
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class DeleteRequestByIdUseCaseTest {
    private lateinit var jsonRpcHistory: JsonRpcHistory
    private lateinit var verifyContextStorageRepository: VerifyContextStorageRepository
    private lateinit var deleteRequestByIdUseCase: DeleteRequestByIdUseCase

    @Before
    fun setup() {
        jsonRpcHistory = mockk(relaxed = true)
        verifyContextStorageRepository = mockk(relaxed = true)
        deleteRequestByIdUseCase = DeleteRequestByIdUseCase(jsonRpcHistory, verifyContextStorageRepository)
    }

    @Test
    fun `invoke should delete record by id from jsonRpcHistory and verifyContextStorageRepository`() = runBlocking {
        val idSlot = slot<Long>()
        val id = 123L

        deleteRequestByIdUseCase.invoke(id)

        coVerify(exactly = 1) { jsonRpcHistory.deleteRecordById(capture(idSlot)) }
        coVerify(exactly = 1) { verifyContextStorageRepository.delete(capture(idSlot)) }
    }
}