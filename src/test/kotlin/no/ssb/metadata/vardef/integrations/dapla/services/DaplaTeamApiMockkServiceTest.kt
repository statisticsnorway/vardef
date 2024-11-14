package no.ssb.metadata.vardef.integrations.dapla.services

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.mockk.*
import io.mockk.impl.annotations.MockK
import no.ssb.metadata.vardef.integrations.dapla.models.Team
import no.ssb.metadata.vardef.integrations.dapla.security.KeycloakService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MockK
class DaplaTeamApiMockkServiceTest {
    private lateinit var mockkDaplaTeamApiClient: DaplaTeamApiClient
    private lateinit var mockkKeycloakService: KeycloakService
    private lateinit var daplaTeamApiService: DaplaTeamApiService
    private lateinit var httpClient: HttpClient

    @BeforeEach
    fun setUp() {
        httpClient = mockk()
        mockkKeycloakService = mockk<KeycloakService>()
        mockkDaplaTeamApiClient = mockk<DaplaTeamApiClient>()
        daplaTeamApiService = DaplaTeamApiService(mockkDaplaTeamApiClient)
        daplaTeamApiService.keycloakService = mockkKeycloakService
    }

    @AfterEach
    internal fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test getTeam should return team successfully`() {
        // Arrange: Mock HttpResponse
        val expectedTeam = Team("dapla-felles")
        val mockResponse: HttpResponse<Team?> = mockk()
        every { mockResponse.status } returns HttpStatus.OK
        every { mockResponse.body() } returns expectedTeam
        every { mockkKeycloakService.requestAccessToken() } returns "Bearer auth"

        // Mock daplaTeamApiClient.fetchTeam to return the mock HttpResponse
        every { mockkDaplaTeamApiClient.fetchTeam(any(), any()) } returns mockResponse

        // Act: Call DaplaApiService method
        val result = daplaTeamApiService.getTeam("dapla-felles")

        // Assert: Verify that the returned team matches the expected team
        assertEquals(expectedTeam, result)
        verify { mockkDaplaTeamApiClient.fetchTeam(any(), any()) }
    }

    @Test
    fun `test getTeam should return null on error`() {
        // Arrange: Mock HttpResponse
        val mockResponse: HttpResponse<Team?> = mockk()
        every { mockResponse.status } returns HttpStatus.NOT_FOUND
        every { mockResponse.body() } returns null
        every { mockkKeycloakService.requestAccessToken() } returns "Bearer auth"

        // Mock daplaTeamApiClient.fetchTeam to return the mock HttpResponse
        every { mockkDaplaTeamApiClient.fetchTeam(any(), any()) } returns mockResponse

        // Act: Call DaplaApiService method
        val result = daplaTeamApiService.getTeam("non-existing-team")

        // Assert: Verify that the result is null due to the 404 response
        assertEquals(null, result)
        verify { mockkDaplaTeamApiClient.fetchTeam(any(), any()) }
    }
}
