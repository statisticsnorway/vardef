package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpResponse
import io.micronaut.http.server.exceptions.HttpServerException
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import no.ssb.metadata.vardef.integrations.klass.models.*
import no.ssb.metadata.vardef.models.SupportedLanguages
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.LocalDateTime

@MockK
class KlassApiServiceTest {
    private lateinit var klassApiMockkClient: KlassApiClient
    private lateinit var klassApiService: KlassApiService
    private lateinit var listClassificationsResponse: KlassApiResponse
    private lateinit var getClassificationResponse: HttpResponse<Classification?>
    private lateinit var codesMock: Codes
    private lateinit var codes: Codes
    private lateinit var codeList: List<Code>
    private val testClassificationId = 1
    private val nonExistingClassificationId = 0

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

    @Property(name = "micronaut.http.services.klass.codes-at")
    private val codesAt: String = ""

    @BeforeEach
    fun setUp() {
        klassApiMockkClient = mockk<KlassApiClient>(relaxed = true)
        klassApiService = KlassApiService(klassApiMockkClient, codesAt)
        codeList =
            listOf(
                Code(code = "1", name = "Ja"),
                Code(code = "2", name = "Nei"),
            )
        listClassificationsResponse =
            KlassApiResponse(
                Classifications(
                    listOf(
                        classification,
                    ),
                ),
            )
        getClassificationResponse = HttpResponse.ok(classification)
        codesMock = mockk<Codes>()
        codes =
            Codes(
                codes = codeList,
            )
    }

    @AfterEach
    internal fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `fetch all classifications from klass api returns 200 OK, but Klass is empty`() {
        every {
            klassApiMockkClient.fetchClassifications()
        } returns
            HttpResponse.ok(
                KlassApiResponse(
                    Classifications(
                        emptyList(),
                    ),
                ),
            )
        val result = klassApiService.getClassifications()
        assertThat(result).hasSize(0)
        verify(exactly = 1) { klassApiMockkClient.fetchClassifications() }
    }

    @Test
    fun `fetch all classifications from klass api returns 200 OK`() {
        every {
            klassApiMockkClient.fetchClassifications()
        } returns HttpResponse.ok(listClassificationsResponse)
        val result = klassApiService.getClassifications()
        assertEquals(1, result.size)
        verify(exactly = 1) { klassApiMockkClient.fetchClassifications() }
    }

    @Test
    fun `fetch all classifications from klass api returns 404 NOT FOUND`() {
        every {
            klassApiMockkClient.fetchClassifications()
        } returns HttpResponse.notFound()

        assertThrows<HttpServerException> {
            klassApiService.getClassifications()
        }

        verify(exactly = 1) { klassApiMockkClient.fetchClassifications() }
    }

    @Test
    fun `fetch all classifications from klass api returns 500 INTERNAL SERVER ERROR`() {
        every {
            klassApiMockkClient.fetchClassifications()
        } returns HttpResponse.serverError()

        assertThrows<HttpServerException> {
            klassApiService.getClassifications()
        }

        verify(exactly = 1) { klassApiMockkClient.fetchClassifications() }
    }

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
            klassApiMockkClient.listCodes(testClassificationId, codesAt)
        } returns
            HttpResponse.ok(
                Codes(
                    codes = emptyList(),
                ),
            )

        assertThrows<NoSuchElementException> {
            klassApiService.getCodeObjectsFor(testClassificationId)
        }

        verify(exactly = 1) { klassApiMockkClient.listCodes(testClassificationId, codesAt) }
    }

    @Test
    fun `fetch code list from klass api returns 200 OK`() {
        every {
            klassApiMockkClient.fetchClassifications()
        } returns HttpResponse.ok(listClassificationsResponse)

        every {
            klassApiMockkClient.listCodes(testClassificationId, codesAt)
        } returns HttpResponse.ok(codes)

        val result = klassApiService.getCodeObjectsFor(testClassificationId)
        verify(exactly = 1) { klassApiMockkClient.listCodes(testClassificationId, codesAt) }
        assertEquals(2, result.size)
    }

    @Test
    fun `fetch classification returns 500 INTERNAL SERVER ERROR`() {
        every {
            klassApiMockkClient.fetchClassification(testClassificationId)
        } returns HttpResponse.serverError()

        assertThrows<NoSuchElementException> {
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
            klassApiMockkClient.fetchClassifications()
        } returns HttpResponse.ok(listClassificationsResponse)

        every {
            klassApiMockkClient.listCodes(testClassificationId, codesAt)
        } returns HttpResponse.ok(codes)

        val result = klassApiService.getCodesFor(testClassificationId.toString())
        verify(exactly = 1) { klassApiMockkClient.listCodes(testClassificationId, codesAt) }
        assertThat(result).containsExactly("1", "2")
    }

    @ParameterizedTest
    @EnumSource(SupportedLanguages::class)
    fun `get code item for language`(language: SupportedLanguages) {
        every {
            klassApiMockkClient.listCodes(testClassificationId, codesAt)
        } returns HttpResponse.ok(codes)

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
