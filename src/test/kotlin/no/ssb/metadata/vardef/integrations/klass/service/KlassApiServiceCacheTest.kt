package no.ssb.metadata.vardef.integrations.klass.service
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.ssb.metadata.vardef.integrations.klass.models.Classification
import no.ssb.metadata.vardef.integrations.klass.models.ClassificationItem
import no.ssb.metadata.vardef.integrations.klass.models.Classifications
import no.ssb.metadata.vardef.integrations.klass.models.KlassApiResponse
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import java.time.LocalDateTime

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KlassApiServiceCacheTest {
    private lateinit var klassApiMockkClient: KlassApiClient
    private lateinit var klassApiService: KlassApiService
    private lateinit var klassApiResponse: KlassApiResponse

    @BeforeAll
    fun setUp() {
        klassApiMockkClient = mockk<KlassApiClient>()
        klassApiService = KlassApiService(klassApiMockkClient)
        klassApiResponse =
            KlassApiResponse(
                Classifications(
                    listOf(
                        Classification(
                            name = "Test",
                            id = 1,
                            classificationType = "classification",
                            lastModified = "${LocalDateTime.now()}",
                            classificationItems =
                                listOf(
                                    ClassificationItem(code = "1", name = "Ja"),
                                    ClassificationItem(code = "2", name = "Nei"),
                                ),
                        ),
                    ),
                ),
            )
        every { klassApiMockkClient.fetchClassifications() } returns (klassApiResponse)
    }

    @AfterAll
    internal fun tearDown() {
        clearAllMocks()
    }

    @Timeout(2)
    @Test
    @Order(1)
    fun `first run cache`() {
        assertEquals(0, klassApiService.classificationCacheSize())
        klassApiService.getClassifications()
        verify(exactly = 1) { klassApiMockkClient.fetchClassifications() }
        assertEquals(1, klassApiService.classificationCacheSize())
    }

    @Timeout(4)
    @Test
    @Order(2)
    fun `second run cache`() {
        klassApiService.getClassifications()
        verify(exactly = 1) { klassApiMockkClient.fetchClassifications() }
        assertEquals(1, klassApiService.classificationCacheSize())
    }
}
