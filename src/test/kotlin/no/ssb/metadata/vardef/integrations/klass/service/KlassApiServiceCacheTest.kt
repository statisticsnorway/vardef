package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpResponse
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.ssb.metadata.vardef.integrations.klass.models.Classification
import no.ssb.metadata.vardef.integrations.klass.models.Code
import no.ssb.metadata.vardef.integrations.klass.models.Codes
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.time.LocalDateTime

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KlassApiServiceCacheTest {
    private lateinit var klassApiMockkClient: KlassApiClient
    private lateinit var klassApiService: KlassApiService
    private lateinit var classification: Classification
    private lateinit var codes: List<Code>
    private val classificationId = 1

    @Property(name = "micronaut.http.services.klass.codes-at")
    private val codesAt: String = ""

    @BeforeAll
    fun setUp() {
        klassApiMockkClient = mockk<KlassApiClient>()
        klassApiService = KlassApiService(klassApiMockkClient, codesAt)
        codes =
            listOf(
                Code(code = "1", name = "Ja"),
                Code(code = "2", name = "Nei"),
            )
        classification =
            Classification(
                name = "Test",
                id = classificationId,
                classificationType = "classification",
                lastModified = "${LocalDateTime.now()}",
                codes = codes,
            )

        every { klassApiMockkClient.fetchClassification(classificationId) } returns HttpResponse.ok(classification)
        every { klassApiMockkClient.listCodes(classificationId, codesAt) } returns HttpResponse.ok(Codes(codes))
    }

    @AfterAll
    internal fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `classifications cache`() {
        assertThat(klassApiService.getClassification(classificationId)).isEqualTo(classification)
        verify(exactly = 1) { klassApiMockkClient.fetchClassification(classificationId) }
        assertThat(klassApiService.getClassification(classificationId)).isEqualTo(classification)
        verify(exactly = 1) { klassApiMockkClient.fetchClassification(classificationId) }
    }

    @Test
    fun `codes cache`() {
        assertThat(klassApiService.getCodeObjectsFor(classificationId)).isEqualTo(codes)
        verify(exactly = 1) { klassApiMockkClient.listCodes(classificationId, codesAt) }
        assertThat(klassApiService.getCodeObjectsFor(classificationId)).isEqualTo(codes)
        verify(exactly = 1) { klassApiMockkClient.listCodes(classificationId, codesAt) }
    }
}
