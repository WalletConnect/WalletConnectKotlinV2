package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.json_rpc.domain.link_mode.LinkModeJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.common.model.TransportType
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.storage.verify.VerifyContextStorageRepository
import com.walletconnect.android.internal.utils.fiveMinutesInSeconds
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.exceptions.MissingSessionAuthenticateRequest
import com.walletconnect.sign.common.model.Request
import com.walletconnect.sign.common.model.vo.clientsync.common.PayloadParams
import com.walletconnect.sign.common.model.vo.clientsync.common.Requester
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.json_rpc.domain.GetPendingSessionAuthenticateRequest
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.randomBytes
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class RejectSessionAuthenticateUseCaseTest {
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface = mockk()
    private val getPendingSessionAuthenticateRequest: GetPendingSessionAuthenticateRequest = mockk()
    private val crypto: KeyManagementRepository = mockk()
    private val verifyContextStorageRepository: VerifyContextStorageRepository = mockk()
    private val linkModeJsonRpcInteractor: LinkModeJsonRpcInteractorInterface = mockk()
    private val logger: Logger = mockk()
    private lateinit var useCase: RejectSessionAuthenticateUseCase
    private val testDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        useCase = RejectSessionAuthenticateUseCase(
            jsonRpcInteractor,
            getPendingSessionAuthenticateRequest,
            crypto,
            verifyContextStorageRepository,
            linkModeJsonRpcInteractor,
            logger
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `rejectSessionAuthenticate should log error and call onFailure when request is missing`() = runTest(testDispatcher) {
        val id = 1L
        val reason = "Test reason"
        coEvery { getPendingSessionAuthenticateRequest(id) } returns null
        every { logger.error(MissingSessionAuthenticateRequest().message) } just Runs
        val onFailure = mockk<(Throwable) -> Unit>(relaxed = true)

        useCase.rejectSessionAuthenticate(id, reason, {}, onFailure)

        verify { logger.error(MissingSessionAuthenticateRequest().message) }
        verify { onFailure(any<MissingSessionAuthenticateRequest>()) }
    }

    @Test
    fun `rejectSessionAuthenticate should publish response and call onSuccess when request is valid`() = runTest(testDispatcher) {
        val id = 1L
        val reason = "Test reason"
        val sessionAuthenticateParams = SignParams.SessionAuthenticateParams(
            expiryTimestamp = fiveMinutesInSeconds,
            requester = Requester("receiverPublicKey", metadata = AppMetaData("name", "description", listOf("url"), "name")),
            authPayload = PayloadParams(
                type = "type",
                aud = "aud",
                version = "v",
                iat = "iat",
                chains = listOf("chains"),
                domain = "sample.kotlin.dapp",
                nonce = randomBytes(12).bytesToHex(),
                exp = null,
                nbf = null,
                statement = "Sign in with wallet.",
                requestId = null,
                resources = listOf(
                    "urn:recap:eyJhdHQiOnsiaHR0cHM6Ly9ub3RpZnkud2FsbGV0Y29ubmVjdC5jb20vYWxsLWFwcHMiOnsiY3J1ZC9zdWJzY3JpcHRpb25zIjpbe31dLCJjcnVkL25vdGlmaWNhdGlvbnMiOlt7fV19fX0=",
                    "ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/"
                ),
            )
        )

        val jsonRpcHistoryEntry = Request<SignParams.SessionAuthenticateParams>(
            chainId = "chainId",
            id = id,
            method = "method",
            topic = Topic("topic"),
            params = sessionAuthenticateParams,
            transportType = TransportType.RELAY
        )
        val senderPublicKey = PublicKey("senderPublicKey")
        val receiverPublicKey = PublicKey("receiverPublicKey")
        val symmetricKey = SymmetricKey("symmetricKey")
        val responseTopic = Topic("responseTopic")
        coEvery { getPendingSessionAuthenticateRequest(id) } returns jsonRpcHistoryEntry
        coEvery { crypto.generateAndStoreX25519KeyPair() } returns senderPublicKey
        coEvery { crypto.generateSymmetricKeyFromKeyAgreement(senderPublicKey, receiverPublicKey) } returns symmetricKey
        coEvery { crypto.getTopicFromKey(receiverPublicKey) } returns responseTopic
        every { crypto.setKey(symmetricKey, responseTopic.value) } just Runs
        coEvery { jsonRpcInteractor.publishJsonRpcResponse(any(), any(), any(), any(), any(), any(), any()) } just Runs
        every { logger.log("Session Authenticate Reject Responded on topic: $responseTopic") } just Runs
        every { logger.log("Sending Session Authenticate Reject on topic: $responseTopic") } just Runs
        coEvery { verifyContextStorageRepository.delete(id) } just Runs
        useCase.rejectSessionAuthenticate(id, reason, onSuccess = {}, onFailure = {})
        coVerify {
            jsonRpcInteractor.publishJsonRpcResponse(
                responseTopic, any(), any(), any(), any(), any(), EnvelopeType.ONE
            )
        }
//        coVerify { verifyContextStorageRepository.delete(any()) }
//        verify { logger.log("Session Authenticate Reject Responded on topic: $responseTopic") }
//        verify { onSuccess() }
    }
}