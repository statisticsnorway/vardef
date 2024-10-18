package no.ssb.metadata.vardef.integrations.vardok

import io.micronaut.context.annotation.Requires
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

@MicronautTest(startApplication = false)
@Requires(env = ["integration-test"])
class VardokApiServiceIntegrationTest {

    @Inject
    lateinit var varDokApiService: VarDokApiService

    @Test
    fun `vardok id not found`() {
        val exception: Exception =
            assertThrows(VardokNotFoundException::class.java) {
                varDokApiService.getVarDokItem("1")
            }
        assertThat(exception).isInstanceOf(VardokNotFoundException::class.java)
        val expectedMessage = "Vardok id 1 not found"
        val actualMessage = exception.message

        assertThat(actualMessage).contains(expectedMessage)
    }

}