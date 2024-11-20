package no.ssb.metadata.vardef.integrations.dapla.services

import io.micronaut.context.annotation.Primary
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.*
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.dapla.models.Group
import no.ssb.metadata.vardef.integrations.dapla.models.Team
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

@MicronautTest
class DaplaTeamApiMockkServiceTest {
    private val mockkDaplaTeamApiClient: DaplaTeamApiClient = mockk<DaplaTeamApiClient>()

    @Inject
    private lateinit var daplaTeamApiService: DaplaTeamApiService

    @Primary
    @MockBean(DaplaTeamApiClient::class)
    fun mockkDaplaTeamApiClient(): DaplaTeamApiClient = mockkDaplaTeamApiClient

    @AfterEach
    internal fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `valid team request`() {
        val expectedTeam = Team("dapla-felles")
        val mockResponse: HttpResponse<Team?> = mockk()
        every { mockResponse.status } returns HttpStatus.OK
        every { mockResponse.body() } returns expectedTeam

        every { mockkDaplaTeamApiClient.fetchTeam(any(), any()) } returns mockResponse

        val resultGetTeam = daplaTeamApiService.getTeam("dapla-felles")
        val resultIsValidTeam = daplaTeamApiService.isValidTeam("play-obr-b")

        assertThat(resultGetTeam).isEqualTo(expectedTeam)
        assertThat(resultIsValidTeam).isEqualTo(true)

        verify(exactly = 2) { mockkDaplaTeamApiClient.fetchTeam(any(), any()) }
    }

    @Test
    fun `invalid team request`() {
        val mockResponse: HttpResponse<Team?> = mockk()
        every { mockResponse.status } returns HttpStatus.NOT_FOUND
        every { mockResponse.body() } returns null

        every { mockkDaplaTeamApiClient.fetchTeam(any(), any()) } returns mockResponse

        val resultGetTeam = daplaTeamApiService.getTeam("non-existing-team")
        val resultIsValidTeam = daplaTeamApiService.isValidTeam("timmy")

        assertThat(resultGetTeam).isEqualTo(null)
        assertThat(resultIsValidTeam).isEqualTo(false)

        verify(exactly = 2) { mockkDaplaTeamApiClient.fetchTeam(any(), any()) }
    }

    @Test
    fun `valid group request`() {
        val expectedGroup = Group("dapla-felles-developers")
        val mockResponse: HttpResponse<Group?> = mockk()
        every { mockResponse.status } returns HttpStatus.OK
        every { mockResponse.body() } returns expectedGroup

        every { mockkDaplaTeamApiClient.fetchGroup(any(), any()) } returns mockResponse

        val resultGetGroup = daplaTeamApiService.getGroup("dapla-felles-developers")
        val resultIsValidGroup = daplaTeamApiService.isValidGroup("play-enhjoern-a-developers")

        assertThat(resultGetGroup).isEqualTo(expectedGroup)
        assertThat(resultIsValidGroup).isEqualTo(true)
        verify { mockkDaplaTeamApiClient.fetchGroup(any(), any()) }
    }

    @Test
    fun `invalid group request`() {
        val mockResponse: HttpResponse<Group?> = mockk()
        every { mockResponse.status } returns HttpStatus.NOT_FOUND
        every { mockResponse.body() } returns null

        every { mockkDaplaTeamApiClient.fetchGroup(any(), any()) } returns mockResponse

        val resultGetGroup = daplaTeamApiService.getGroup("non-existing-group-developers")
        val resultIsValidGroup = daplaTeamApiService.isValidGroup("group-managers")

        assertThat(resultGetGroup).isEqualTo(null)
        assertThat(resultIsValidGroup).isEqualTo(false)
        verify { mockkDaplaTeamApiClient.fetchGroup(any(), any()) }
    }
}
