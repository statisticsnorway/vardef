package no.ssb.metadata.vardef.integrations.vardok

import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MockK
class VardokServiceTest {
    private lateinit var varDokClient: VarDokClient
    private lateinit var varDokService: VarDokService
    private lateinit var vardokResponse: VardokResponse

    @BeforeEach
    fun setUp() {
        varDokClient = mockk<VarDokClient>(relaxed = true)
        varDokService = VarDokService(varDokClient)
        vardokResponse = vardokResponseOk
    }

    @AfterEach
    internal fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `get vardok with valid and data element name returns 200 OK`() {
        every {
            varDokClient.fetchVarDokById("2")
        } returns
            vardokResponse
        val result = varDokService.getVarDokItem("2")
        assertThat(result).isEqualTo(vardokResponse)
    }

    @Test
    fun `get vardok with invalid id`() {
        every {
            varDokClient.fetchVarDokById("1")
        } throws
            HttpStatusException(HttpStatus.NOT_FOUND, "Id not found")
        // val result = varDokService.getVarDokItem("1")
        // assertThat(result).isNull()
    }
}
