package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.context.annotation.Primary
import io.micronaut.http.HttpResponse
import io.micronaut.http.server.exceptions.HttpServerException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.inject.Inject
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.models.Classification
import no.ssb.metadata.vardef.integrations.klass.models.Code
import no.ssb.metadata.vardef.integrations.klass.models.Codes
import no.ssb.metadata.vardef.models.SupportedLanguages
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.LocalDate
import java.time.LocalDateTime

@MicronautTest(startApplication = false)
class KlassApiServiceTest {
    @Inject
    lateinit var klassApiService: KlassApiService

    @Inject
    private lateinit var klassApiMockkClient: KlassApiClient

    private val codesAt = LocalDate.now().toString()

    @Primary
    @Singleton
    @MockBean(KlassApiClient::class)
    fun mockKlassApiClient(): KlassApiClient = mockk<KlassApiClient>()

    private val codeList =
        listOf(
            Code(code = "1", name = "Ja"),
            Code(code = "2", name = "Nei"),
        )
    private val codes =
        Codes(
            codes = codeList,
        )
    private val testClassificationId = 1
    private val nonExistingClassificationId = 0
    private val language = SupportedLanguages.NB

    private val classification =
        Classification(
            name = "Test",
            id = testClassificationId,
            classificationType = "classification",
            lastModified = "${LocalDateTime.now()}",
            codes =
                listOf(
                    Code(code = "1", name = "Ja"),
                    Code(code = "2", name = "Nei"),
                ),
        )

    private val getClassificationResponse: HttpResponse<Classification?> = HttpResponse.ok(classification)
    private val listCodesResponse: HttpResponse<Codes> = HttpResponse.ok(codes)

    @BeforeEach
    fun invalidateCaches(): Unit = klassApiService.invalidateCaches()

    @Test
    fun `get non-existing classification returns exception`() {
        every {
            klassApiMockkClient.fetchClassification(nonExistingClassificationId)
        } returns HttpResponse.notFound()

        assertThrows<NoSuchElementException> {
            klassApiService.getClassification(nonExistingClassificationId)
        }

        verify(exactly = 1) { klassApiMockkClient.fetchClassification(nonExistingClassificationId) }
    }

    @Test
    fun `fetch code list no codes returned`() {
        every {
            klassApiMockkClient.listCodes(testClassificationId, codesAt, language)
        } returns
            HttpResponse.ok(
                Codes(
                    codes = emptyList(),
                ),
            )

        assertThrows<NoSuchElementException> {
            klassApiService.getCodeObjectsFor(testClassificationId, language)
        }

        verify(exactly = 1) { klassApiMockkClient.listCodes(testClassificationId, codesAt, language) }
    }

    @Test
    fun `fetch code list from klass api returns 200 OK`() {
        every {
            klassApiMockkClient.listCodes(testClassificationId, codesAt, language)
        } returns listCodesResponse

        val result = klassApiService.getCodeObjectsFor(testClassificationId, language)
        verify(exactly = 1) { klassApiMockkClient.listCodes(testClassificationId, codesAt, language) }
        assertEquals(2, result.size)
    }

    @Test
    fun `fetch classification returns 500 INTERNAL SERVER ERROR`() {
        every {
            klassApiMockkClient.fetchClassification(testClassificationId)
        } returns HttpResponse.serverError()

        assertThrows<HttpServerException> {
            klassApiService.getClassification(testClassificationId)
        }

        verify(exactly = 1) { klassApiMockkClient.fetchClassification(testClassificationId) }
    }

    @Test
    fun `fetch non-existent classification`() {
        every {
            klassApiMockkClient.fetchClassification(testClassificationId)
        } returns HttpResponse.notFound()

        assertThrows<NoSuchElementException> {
            klassApiService.getClassification(testClassificationId)
        }

        verify(exactly = 1) { klassApiMockkClient.fetchClassification(testClassificationId) }
    }

    @Test
    fun `get existing classification by id`() {
        every {
            klassApiMockkClient.fetchClassification(testClassificationId)
        } returns getClassificationResponse

        val result = klassApiService.getClassification(testClassificationId)
        verify(exactly = 1) { klassApiMockkClient.fetchClassification(testClassificationId) }

        assertThat(result).isInstanceOf(Classification::class.java)
        assertThat(result.codes).hasSize(2)
    }

    @Test
    fun `get codes for returns a list of just the codes`() {
        every {
            klassApiMockkClient.listCodes(testClassificationId, codesAt, language)
        } returns listCodesResponse

        val result = klassApiService.getCodesFor(testClassificationId.toString())
        verify(exactly = 1) { klassApiMockkClient.listCodes(testClassificationId, codesAt, language) }
        assertThat(result).containsExactly("1", "2")
    }

    @ParameterizedTest
    @EnumSource(SupportedLanguages::class)
    fun `get code item for language`(language: SupportedLanguages) {
        every {
            klassApiMockkClient.listCodes(testClassificationId, codesAt, language)
        } returns listCodesResponse

        assertThat(
            klassApiService
                .renderCode(
                    testClassificationId.toString(),
                    "1",
                    language,
                )?.title,
        ).isNotNull()
    }
}
