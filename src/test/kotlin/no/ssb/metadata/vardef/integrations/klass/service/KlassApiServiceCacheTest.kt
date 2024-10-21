package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.klass.models.Classification
import no.ssb.metadata.vardef.integrations.klass.models.Code
import no.ssb.metadata.vardef.integrations.klass.models.Codes
import no.ssb.metadata.vardef.models.SupportedLanguages
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@MicronautTest
class KlassApiServiceCacheTest {
    @Inject
    lateinit var klassApiService: KlassApiService

    @Inject
    private lateinit var klassApiMockkClient: KlassApiClient

    @Property(name = "klass.codes-at")
    private lateinit var codesAt: String

    private val classificationId = 1
    private val language = SupportedLanguages.NB
    private val codes =
        listOf(
            Code(code = "1", name = "Ja"),
            Code(code = "2", name = "Nei"),
        )
    private val classification =
        Classification(
            name = "Test",
            id = classificationId,
            classificationType = "classification",
            lastModified = "${LocalDateTime.now()}",
            codes = codes,
        )

    @Primary
    @MockBean(KlassApiClient::class)
    fun mockKlassApiClient(): KlassApiClient {
        val klassApiMockkClient = mockk<KlassApiClient>()
        every { klassApiMockkClient.fetchClassification(classificationId) } returns HttpResponse.ok(classification)
        every { klassApiMockkClient.listCodes(classificationId, codesAt, language) } returns HttpResponse.ok(Codes(codes))
        return klassApiMockkClient
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
        assertThat(klassApiService.getCodeObjectsFor(classificationId, language)).isEqualTo(codes)
        verify(exactly = 1) { klassApiMockkClient.listCodes(classificationId, codesAt, language) }
        assertThat(klassApiService.getCodeObjectsFor(classificationId, language)).isEqualTo(codes)
        verify(exactly = 1) { klassApiMockkClient.listCodes(classificationId, codesAt, language) }
    }
}
