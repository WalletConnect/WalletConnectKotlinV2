package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.RequestExpiredException
import com.walletconnect.android.internal.common.json_rpc.domain.link_mode.LinkModeJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.common.model.TransportType
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.signing.cacao.CacaoType
import com.walletconnect.android.internal.common.signing.cacao.CacaoVerifier
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.common.storage.verify.VerifyContextStorageRepository
import com.walletconnect.android.internal.utils.fiveMinutesInSeconds
import com.walletconnect.android.pulse.domain.InsertEventUseCase
import com.walletconnect.android.pulse.domain.InsertTelemetryEventUseCase
import com.walletconnect.android.pulse.model.Trace
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.exceptions.MissingSessionAuthenticateRequest
import com.walletconnect.sign.common.model.Request
import com.walletconnect.sign.common.model.vo.clientsync.common.PayloadParams
import com.walletconnect.sign.common.model.vo.clientsync.common.Requester
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.json_rpc.domain.GetPendingSessionAuthenticateRequest
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.randomBytes
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.invoke
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ApproveSessionAuthenticateUseCaseTest {
    private lateinit var useCase: ApproveSessionAuthenticateUseCase
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface = mockk(relaxed = true)
    private val getPendingSessionAuthenticateRequest: GetPendingSessionAuthenticateRequest = mockk(relaxed = true)
    private val crypto: KeyManagementRepository = mockk(relaxed = true)
    private val cacaoVerifier: CacaoVerifier = mockk(relaxed = true)
    private val verifyContextStorageRepository: VerifyContextStorageRepository = mockk(relaxed = true)
    private val logger: Logger = mockk(relaxed = true)
    private val metadataStorageRepository: MetadataStorageRepositoryInterface = mockk(relaxed = true)
    private val selfAppMetaData: AppMetaData = mockk(relaxed = true)
    private val sessionStorageRepository: SessionStorageRepository = mockk(relaxed = true)
    private val insertEventUseCase: InsertEventUseCase = mockk(relaxed = true)
    private val insertTelemetryEventUseCase: InsertTelemetryEventUseCase = mockk(relaxed = true)
    private val linkModeJsonRpcInteractor: LinkModeJsonRpcInteractorInterface = mockk(relaxed = true)
    private val iss = "did:pkh:eip155:1:0x15bca56b6e2728aec2532df9d436bd1600e86688"
    private val payload = Cacao.Payload(
        iss = iss,
        domain = "service.invalid",
        aud = "https://service.invalid/login",
        version = "1",
        nonce = "32891756",
        iat = "2021-09-30T16:25:24Z",
        nbf = null,
        exp = null,
        statement = "I accept the ServiceOrg Terms of Service: https://service.invalid/tos",
        requestId = null,
        resources =
        listOf(
            "urn:recap:eyJhdHQiOnsiZWlwMTU1Ijp7InJlcXVlc3QvcGVyc29uYWxfc2lnbiI6W3siY2hhaW5zIjpbImVpcDE1NToxIl19XSwicmVxdWVzdC9ldGhfc2lnblR5cGVkRGF0YV92NCI6W3siY2hhaW5zIjpbImVpcDE1NToxIl19XX19fQ"
        )
    )

    @Before
    fun setUp() {
        useCase = ApproveSessionAuthenticateUseCase(
            jsonRpcInteractor,
            getPendingSessionAuthenticateRequest,
            crypto,
            cacaoVerifier,
            verifyContextStorageRepository,
            logger,
            metadataStorageRepository,
            selfAppMetaData,
            sessionStorageRepository,
            insertTelemetryEventUseCase,
            insertEventUseCase,
            "clientID",
            linkModeJsonRpcInteractor
        )
    }

    @Test
    fun `approveSessionAuthenticate successfully processes a valid request`() = runTest {
        val id = 123L
        val symmetricKey = mockk<SymmetricKey>(relaxed = true)
        val receiverKey = "receiverKey"
        val sessionTopic = Topic("sessionTopic")
        val responseTopic = Topic("responseTopic")
        val publicKey = mockk<PublicKey>(relaxed = true)
        val sessionAuthenticateParams = SignParams.SessionAuthenticateParams(
            expiryTimestamp = fiveMinutesInSeconds,
            requester = Requester(receiverKey, metadata = AppMetaData("name", "description", listOf("url"), "name")),
            authPayload = PayloadParams(
                type = "type",
                aud = "aud",
                version = "v",
                iat = "iat",
                chains = listOf("eip155:1"),
                domain = "sample.kotlin.dapp",
                nonce = randomBytes(12).bytesToHex(),
                exp = null,
                nbf = null,
                statement = "Sign in with wallet.",
                requestId = null,
                resources = listOf(
                    "urn:recap:eyJhdHQiOnsiZWlwMTU1Ijp7InJlcXVlc3QvcGVyc29uYWxfc2lnbiI6W3siY2hhaW5zIjpbImVpcDE1NToxIl19XSwicmVxdWVzdC9ldGhfc2lnblR5cGVkRGF0YV92NCI6W3siY2hhaW5zIjpbImVpcDE1NToxIl19XX19fQ"
                ),
            )
        )
        val cacaos = listOf(Cacao(CacaoType.CAIP222.toHeader(), payload, mockk()))
        val jsonRpcHistoryEntry = Request(
            chainId = "chainId",
            id = id,
            method = "method",
            topic = Topic("topic"),
            params = sessionAuthenticateParams,
            transportType = TransportType.RELAY
        )

        every { getPendingSessionAuthenticateRequest(id) } returns jsonRpcHistoryEntry
        every { crypto.generateAndStoreX25519KeyPair() } returns publicKey
        every { crypto.generateSymmetricKeyFromKeyAgreement(any(), any()) } returns symmetricKey
        every { crypto.getTopicFromKey(symmetricKey) } returns sessionTopic
        every { crypto.getTopicFromKey(PublicKey("receiverKey")) } returns responseTopic
        every { cacaoVerifier.verify(any()) } returns true
        every { jsonRpcInteractor.subscribe(any(), any(), any()) } just Runs
        every { jsonRpcInteractor.publishJsonRpcResponse(any(), any(), any(), any(), any(), any()) }
        coEvery { verifyContextStorageRepository.delete(id) } just Runs

        useCase.approveSessionAuthenticate(id, cacaos, onSuccess = {}, onFailure = { assert(false) })

        verify { logger.log(Trace.SessionAuthenticate.SESSION_AUTHENTICATE_APPROVE_STARTED) }
        coVerify {
            jsonRpcInteractor.publishJsonRpcResponse(
                eq(responseTopic),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        }
    }

    @Test
    fun `approveSessionAuthenticate throws exception when session authenticate request is missing`() = runTest {
        val id = 123L

        every { getPendingSessionAuthenticateRequest(id) } returns null

        useCase.approveSessionAuthenticate(id, listOf(), onSuccess = {
            assert(false)
        }, onFailure = { throwable ->
            assert(throwable is MissingSessionAuthenticateRequest)
        })

        coVerify { insertTelemetryEventUseCase(any()) }
    }

    @Test
    fun `approveSessionAuthenticate fails if request is expired`() = runTest {
        val id = 123L
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
            transportType = TransportType.RELAY,
            expiry = Expiry(0L)
        )

        every { getPendingSessionAuthenticateRequest(id) } returns jsonRpcHistoryEntry

        useCase.approveSessionAuthenticate(id, listOf(), onSuccess = {
            assert(false)
        }, onFailure = { throwable ->
            assert(throwable is RequestExpiredException)
        })

        coVerify { insertTelemetryEventUseCase(any()) }
    }

    @Test
    fun `approveSessionAuthenticate handles exception in publishing session response`() = runTest {
        val id = 123L
        val symmetricKey = mockk<SymmetricKey>(relaxed = true)
        val receiverKey = "receiverKey"
        val sessionTopic = Topic("sessionTopic")
        val responseTopic = Topic("responseTopic")
        val publicKey = mockk<PublicKey>(relaxed = true)
        val sessionAuthenticateParams = SignParams.SessionAuthenticateParams(
            expiryTimestamp = fiveMinutesInSeconds,
            requester = Requester(receiverKey, metadata = AppMetaData("name", "description", listOf("url"), "name")),
            authPayload = PayloadParams(
                type = "type",
                aud = "aud",
                version = "v",
                iat = "iat",
                chains = listOf("eip155:1"),
                domain = "sample.kotlin.dapp",
                nonce = randomBytes(12).bytesToHex(),
                exp = null,
                nbf = null,
                statement = "Sign in with wallet.",
                requestId = null,
                resources = listOf(
                    "urn:recap:eyJhdHQiOnsiZWlwMTU1Ijp7InJlcXVlc3QvcGVyc29uYWxfc2lnbiI6W3siY2hhaW5zIjpbImVpcDE1NToxIl19XSwicmVxdWVzdC9ldGhfc2lnblR5cGVkRGF0YV92NCI6W3siY2hhaW5zIjpbImVpcDE1NToxIl19XX19fQ"
                ),
            )
        )
        val cacaos = listOf(Cacao(CacaoType.CAIP222.toHeader(), payload, mockk()))
        val jsonRpcHistoryEntry = Request(
            chainId = "chainId",
            id = id,
            method = "method",
            topic = Topic("topic"),
            params = sessionAuthenticateParams,
            transportType = TransportType.RELAY
        )

        every { getPendingSessionAuthenticateRequest(id) } returns jsonRpcHistoryEntry
        every { crypto.generateAndStoreX25519KeyPair() } returns publicKey
        every { crypto.generateSymmetricKeyFromKeyAgreement(any(), any()) } returns symmetricKey
        every { crypto.getTopicFromKey(symmetricKey) } returns sessionTopic
        every { crypto.getTopicFromKey(PublicKey("receiverKey")) } returns responseTopic
        every { cacaoVerifier.verify(any()) } returns true
        every { jsonRpcInteractor.subscribe(any(), any(), any()) } just Runs
        coEvery { verifyContextStorageRepository.delete(id) } just Runs
        every { jsonRpcInteractor.publishJsonRpcResponse(any(), any(), any(), any(), captureLambda(), any(), any()) } answers {
            lambda<(Throwable) -> Unit>().invoke(mockk())
        }

        useCase.approveSessionAuthenticate(id, cacaos, onSuccess = {
            assert(false)
        }, onFailure = {
            assert(true)
        })
    }
}