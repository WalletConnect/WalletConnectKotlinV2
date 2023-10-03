package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.exceptions.InvalidNamespaceException
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.storage.proposal.ProposalStorageRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class ProposeSessionUseCaseTest {
    private val jsonRpcInteractor = mockk<JsonRpcInteractorInterface>()
    private val crypto = mockk<KeyManagementRepository>()
    private val proposalStorageRepository = mockk<ProposalStorageRepository>()
    private val selfAppMetaData = mockk<AppMetaData>()
    private val logger = mockk<Logger>()
    private val proposeSessionUseCase = ProposeSessionUseCase(jsonRpcInteractor, crypto, proposalStorageRepository, selfAppMetaData, logger)

    @Before
    fun setUp() {
        every { logger.error(any() as String) } answers { }
        every { logger.error(any() as Exception) } answers { }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `onFailure is called when SignValidator validateProposalNamespaces fails`() = runTest {

        proposeSessionUseCase.proposeSession(
            requiredNamespaces = mapOf("required" to EngineDO.Namespace.Proposal(listOf("required"), listOf("required"), listOf("required"))),
            optionalNamespaces = mapOf("optional" to EngineDO.Namespace.Proposal(listOf("optional"), listOf("optional"), listOf("optional"))),
            properties = mapOf("key" to "value"),
            pairing = com.walletconnect.android.internal.common.model.Pairing(
                topic = Topic("topic"),
                relay = RelayProtocolOptions(),
                symmetricKey = SymmetricKey("symmetricKey"),
                registeredMethods = ""
            ),
            onSuccess = {
                fail("onSuccess should not be called since should have validation failed")
            },
            onFailure = { error ->
                assertSame(InvalidNamespaceException::class, error::class)
            }
        )
    }
}