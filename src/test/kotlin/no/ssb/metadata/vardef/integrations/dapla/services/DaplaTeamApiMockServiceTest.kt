package no.ssb.metadata.vardef.integrations.dapla.services

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import no.ssb.metadata.vardef.integrations.dapla.models.Team
import no.ssb.metadata.vardef.integrations.dapla.security.KeycloakService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MockK
class DaplaTeamApiMockServiceTest {

    private lateinit var daplaTeamApiClient: DaplaTeamApiClient
    private lateinit var keycloakService: KeycloakService
    private lateinit var daplaTeamApiService: DaplaTeamApiService
    private lateinit var httpClient: HttpClient

    private lateinit var mockkDaplaTeamApiClient: DaplaTeamApiClient

    @BeforeEach
    fun setUp() {
        httpClient = mockk()
        keycloakService = mockk()
        mockkDaplaTeamApiClient= mockk<DaplaTeamApiClient>()
        daplaTeamApiService =  DaplaTeamApiService(mockkDaplaTeamApiClient)
        daplaTeamApiService.keycloakService = keycloakService
    }

    @AfterEach
    internal fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `get team from dapla`() {
        every {
            mockkDaplaTeamApiClient.fetchTeam("dapla-felles", "Bearer auth")
        } returns
                HttpResponse.ok()
                Team("dapla-felles")
    }

    @Test
    fun `test getTeam should return team successfully`() {
        // Arrange: Mocking HttpResponse
        val expectedTeam = Team("team-name")
        val mockResponse: HttpResponse<Team?> = mockk()
        every { mockResponse.status } returns HttpStatus.OK
        every { mockResponse.body() } returns expectedTeam

        // Mock daplaTeamApiClient.fetchTeam to return the mock HttpResponse
        every { daplaTeamApiClient.fetchTeam(any(), any()) } returns mockResponse

        // Act: Call the method under test
        val result = daplaTeamApiService.getTeam("team-name")

        // Assert: Verify that the returned team matches the expected team
        assertEquals(expectedTeam, result)
        verify { daplaTeamApiClient.fetchTeam(any(), any()) }
    }

    @Test
    fun `test getTeam should return null on error`() {
        // Arrange: Mocking HttpResponse for error case
        val mockResponse: HttpResponse<Team?> = mockk()
        every { mockResponse.status } returns HttpStatus.NOT_FOUND
        every { mockResponse.body() } returns null

        // Mock daplaTeamApiClient.fetchTeam to return the mock HttpResponse
        every { daplaTeamApiClient.fetchTeam(any(), any()) } returns mockResponse

        // Act: Call the method under test
        val result = daplaTeamApiService.getTeam("non-existing-team")

        // Assert: Verify that the result is null due to the 404 response
        assertEquals(null, result)
        verify { daplaTeamApiClient.fetchTeam(any(), any()) }
    }

}