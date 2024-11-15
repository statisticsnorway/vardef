package no.ssb.metadata.vardef.integrations.dapla.security

import io.micronaut.http.client.HttpClient
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

@MockK
class KeycloakMockkServiceTest {
    private lateinit var keycloakService: KeycloakService
    private lateinit var httpClient: HttpClient

    @BeforeEach
    fun setUp() {
        httpClient = mockk()
        keycloakService = KeycloakService(httpClient)
        keycloakService.clientId = "test-client-id"
    }

    @AfterEach
    internal fun tearDown() {
        clearAllMocks()
    }
}
