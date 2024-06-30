package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.http.HttpResponse
import io.micronaut.http.server.exceptions.HttpServerException
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import no.ssb.metadata.vardef.integrations.klass.models.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

@MockK
class KlassApiServiceTest {
    private lateinit var klassApiMockkClient: KlassApiClient
    private lateinit var klassApiService: KlassApiService
    private lateinit var klassApiResponse: KlassApiResponse
    private lateinit var klassApiCodeListResponseMock: KlassApiCodeListResponse
    private lateinit var klassApiCodeListResponse: KlassApiCodeListResponse
    private lateinit var codeList: List<ClassificationItem>
    private val testClassificationId = 1
    private val nonExistingClassificationId = 0

    @BeforeEach
    fun setUp() {
        klassApiMockkClient = mockk<KlassApiClient>(relaxed = true)
        klassApiService = KlassApiService(klassApiMockkClient)
        codeList =
            listOf(
                ClassificationItem(code = "1", name = "Ja"),
                ClassificationItem(code = "2", name = "Nei"),
            )
        klassApiResponse =
            KlassApiResponse(
                Classifications(
                    listOf(
                        Classification(
                            name = "Test",
                            id = testClassificationId,
                            classificationType = "classification",
                            lastModified = "${LocalDateTime.now()}",
                        ),
                    ),
                ),
            )
        klassApiCodeListResponseMock = mockk<KlassApiCodeListResponse>()
        klassApiCodeListResponse =
            KlassApiCodeListResponse(
                classificationItems = codeList,
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
        val result = klassApiService.fetchAllClassifications()
        assertThat(result).isNotNull
        assertThat(result).hasSize(0)
        verify(exactly = 1) { klassApiMockkClient.fetchClassifications() }
    }

    @Test
    fun `fetch all classifications from klass api returns 200 OK`() {
        every {
            klassApiMockkClient.fetchClassifications()
        } returns HttpResponse.ok(klassApiResponse)
        val result = klassApiService.fetchAllClassifications()
        assertThat(result).isNotNull
        assertEquals(1, result.size)
        verify(exactly = 1) { klassApiMockkClient.fetchClassifications() }
    }

    @Test
    fun `fetch all classifications from klass api returns 404 NOT FOUND`() {
        every {
            klassApiMockkClient.fetchClassifications()
        } returns HttpResponse.notFound()

        assertThrows<HttpServerException> {
            klassApiService.fetchAllClassifications()
        }

        verify(exactly = 1) { klassApiMockkClient.fetchClassifications() }
    }

    @Test
    fun `fetch all classifications from klass api returns 500 INTERNAL SERVER ERROR`() {
        every {
            klassApiMockkClient.fetchClassifications()
        } throws HttpServerException("Error while fetching classifications from Klass Api")

        assertThrows<HttpServerException> {
            klassApiService.fetchAllClassifications()
        }

        verify(exactly = 1) { klassApiMockkClient.fetchClassifications() }
    }

    @Test
    fun `get non-existing classification by id from cache returns exception`() {
        every {
            klassApiMockkClient.fetchClassifications()
        } returns HttpResponse.ok(klassApiResponse)

        assertThrows<NoSuchElementException> {
            klassApiService.getClassification(nonExistingClassificationId)
        }

        verify(exactly = 1) { klassApiMockkClient.fetchClassifications() }
    }

    @Test
    fun `fetch mocked code list fra klass api`() {
        every {
            klassApiMockkClient.fetchClassifications()
        } returns HttpResponse.ok(klassApiResponse)

        every {
            klassApiMockkClient.fetchCodeList(0)
        } returns HttpResponse.notFound(klassApiCodeListResponse)

        assertThrows<NoSuchElementException> {
            klassApiService.getClassification(1)
        }

        verify(exactly = 1) { klassApiMockkClient.fetchCodeList(testClassificationId) }
    }

    @Test
    fun `fetch mocked code list from klass api returns no content`() {
        every {
            klassApiMockkClient.fetchClassifications()
        } returns HttpResponse.ok(klassApiResponse)

        every {
            klassApiMockkClient.fetchCodeList(nonExistingClassificationId)
        } returns HttpResponse.noContent()

        assertThrows<NoSuchElementException> {
            klassApiService.getClassification(nonExistingClassificationId)
        }

        verify(exactly = 0) { klassApiMockkClient.fetchCodeList(nonExistingClassificationId) }
    }

    @Test
    fun `fetch code list fra klass api`() {
        every {
            klassApiMockkClient.fetchClassifications()
        } returns HttpResponse.ok(klassApiResponse)
        every {
            klassApiMockkClient.fetchCodeList(testClassificationId)
        } returns HttpResponse.ok(klassApiCodeListResponse)
        assertEquals(0, klassApiService.classificationItemListCache())
        val result = klassApiService.getClassificationItemsById(testClassificationId)
        verify(exactly = 1) { klassApiMockkClient.fetchCodeList(testClassificationId) }
        assertThat(result).isNotNull
        assertEquals(2, result.size)
        assertEquals(1, klassApiService.classificationItemListCache())
    }

    @Test
    fun `fetch non-existing code list fra klass api`() {
        every {
            klassApiMockkClient.fetchClassifications()
        } returns HttpResponse.ok(klassApiResponse)

        every {
            klassApiMockkClient.fetchCodeList(testClassificationId)
        } returns
            HttpResponse.ok(
                KlassApiCodeListResponse(
                    classificationItems = emptyList(),
                ),
            )

        assertThrows<NoSuchElementException> {
            klassApiService.getClassification(testClassificationId)
        }

        verify(exactly = 1) { klassApiMockkClient.fetchCodeList(testClassificationId) }
    }
}
