package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.InvalidExpiryException
import com.walletconnect.android.internal.common.json_rpc.domain.link_mode.LinkModeJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.Namespace
import com.walletconnect.android.internal.common.model.Redirect
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.currentTimeInSeconds
import com.walletconnect.android.internal.utils.fiveMinutesInSeconds
import com.walletconnect.android.pulse.domain.InsertEventUseCase
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.storage.authenticate.AuthenticateResponseTopicRepository
import com.walletconnect.sign.storage.link_mode.LinkModeStorageRepository
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SessionAuthenticateUseCaseTest {
    private var jsonRpcInteractor: RelayJsonRpcInteractorInterface = mockk()
    private var crypto: KeyManagementRepository = mockk()
    private var selfAppMetaData: AppMetaData = AppMetaData(
        description = "description",
        url = "url",
        icons = emptyList(),
        name = "name",
        redirect = Redirect(linkMode = true, universal = "universal"),
        verifyUrl = null,
    )
    private var authenticateResponseTopicRepository: AuthenticateResponseTopicRepository = mockk()
    private var proposeSessionUseCase: ProposeSessionUseCaseInterface = mockk()
    private var getPairingForSessionAuthenticate: GetPairingForSessionAuthenticateUseCase = mockk()
    private var getNamespacesFromReCaps: GetNamespacesFromReCaps = mockk()
    private var linkModeJsonRpcInteractor: LinkModeJsonRpcInteractorInterface = mockk()
    private var linkModeStorageRepository: LinkModeStorageRepository = mockk()
    private var insertEventUseCase: InsertEventUseCase = mockk()
    private var logger: Logger = mockk()
    private val clientId: String = "testClientId"
    private lateinit var sessionAuthenticateUseCase: SessionAuthenticateUseCase

    @Before
    fun setup() {
        sessionAuthenticateUseCase = SessionAuthenticateUseCase(
            jsonRpcInteractor = jsonRpcInteractor,
            crypto = crypto,
            selfAppMetaData = selfAppMetaData,
            authenticateResponseTopicRepository = authenticateResponseTopicRepository,
            proposeSessionUseCase = proposeSessionUseCase,
            getPairingForSessionAuthenticate = getPairingForSessionAuthenticate,
            getNamespacesFromReCaps = getNamespacesFromReCaps,
            linkModeJsonRpcInteractor = linkModeJsonRpcInteractor,
            linkModeStorageRepository = linkModeStorageRepository,
            insertEventUseCase = insertEventUseCase,
            logger = logger,
            clientId = clientId
        )
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should fail when chains are empty`() = runTest {
        val authenticate = EngineDO.Authenticate(
            pairingTopic = "topic",
            chains = emptyList(),
            domain = "domain",
            nonce = "nonce",
            aud = "aud",
            type = null,
            nbf = null,
            exp = null,
            statement = null,
            requestId = "id",
            resources = listOf("resources"),
            methods = emptyList(),
            expiry = null
        )
        val onFailure: (Throwable) -> Unit = mockk(relaxed = true)
        every { logger.error(any<String>()) } just Runs

        sessionAuthenticateUseCase.authenticate(
            authenticate,
            methods = null,
            pairingTopic = null,
            expiry = null,
            walletAppLink = null,
            onSuccess = {},
            onFailure = onFailure
        )

        verify { logger.error("Sending session authenticate request error: chains are empty") }
        verify { onFailure(any<IllegalArgumentException>()) }
    }

    @Test
    fun `should fail when expiry is not within bounds`() = runTest {
        val authenticate = EngineDO.Authenticate(
            pairingTopic = "topic",
            chains = listOf("1"),
            domain = "domain",
            nonce = "nonce",
            aud = "aud",
            type = null,
            nbf = null,
            exp = null,
            statement = null,
            requestId = "id",
            resources = listOf("resources"),
            methods = emptyList(),
            expiry = null
        )
        every { logger.error(any<String>()) } just Runs
        val onFailure: (Throwable) -> Unit = mockk(relaxed = true)

        sessionAuthenticateUseCase.authenticate(
            authenticate,
            methods = null,
            pairingTopic = null,
            expiry = Expiry(0),
            walletAppLink = null,
            onSuccess = {},
            onFailure = onFailure
        )

        verify { logger.error("Sending session authenticate request error: expiry not within bounds") }
        verify { onFailure(any<InvalidExpiryException>()) }
    }

    @Test
    fun `should succeed when all parameters are valid`() = runTest {
        val expiryRequest = currentTimeInSeconds + fiveMinutesInSeconds
        val authenticate = EngineDO.Authenticate(
            pairingTopic = "topic",
            chains = listOf("eip155:1"),
            domain = "domain",
            nonce = "nonce",
            aud = "aud",
            type = null,
            nbf = null,
            exp = null,
            statement = null,
            requestId = "id",
            resources = listOf("ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/"),
            methods = emptyList(),
            expiry = expiryRequest
        )
        every { crypto.generateAndStoreX25519KeyPair() } returns PublicKey("pubKey")
        every { crypto.getTopicFromKey(any()) } returns Topic("topic")
        every { crypto.setKey(any(), any()) } just Runs
        every { getNamespacesFromReCaps(any(), any()) } returns mapOf(
            "eip155" to Namespace.Proposal(
                events = listOf("chainChanged", "accountsChanged"),
                methods = listOf("personal_sign"),
                chains = listOf("eip155:1")
            )
        )
        every { logger.log(any<String>()) } just Runs
        coEvery { linkModeStorageRepository.isEnabled(any()) } returns true
        coEvery { jsonRpcInteractor.subscribe(any(), any(), any()) } just Runs
        coEvery { getPairingForSessionAuthenticate(any()) } returns Core.Model.Pairing(
            topic = "topic",
            peerAppMetaData = Core.Model.AppMetaData("name", "description", "url", listOf("icons"), "redirect", "verifyUrl"),
            relayData = null,
            relayProtocol = "relayProtocol",
            uri = "uri",
            isActive = true,
            registeredMethods = "wc_sessionAuthenticate",
            expiry = currentTimeInSeconds + fiveMinutesInSeconds
        )
        coEvery { linkModeJsonRpcInteractor.triggerRequest(any(), any(), any(), any()) } just Runs
        coEvery { insertEventUseCase(any()) } just Runs

        val onSuccess: (String) -> Unit = mockk(relaxed = true)
        val onFailure: (Throwable) -> Unit = mockk(relaxed = true)

        sessionAuthenticateUseCase.authenticate(
            authenticate,
            methods = listOf("testMethods"),
            pairingTopic = "testPairingTopic",
            expiry = Expiry(currentTimeInSeconds + fiveMinutesInSeconds),
            walletAppLink = "applink",
            onSuccess = onSuccess,
            onFailure = onFailure
        )

        every {
            logger.log("\"Link Mode - Request triggered successfully")
        }
    }

    //todo: test case for Relay flow
}