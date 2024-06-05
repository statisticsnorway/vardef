package no.ssb.metadata

import io.micronaut.context.annotation.Property
import io.micronaut.core.util.StringUtils
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@Property(name = "micronaut.http.client.follow-redirects", value = StringUtils.FALSE)
@MicronautTest
internal class HomeControllerTest {
    @Test
    fun testRedirectionToApiDocs(
        @Client("/") httpClient: HttpClient,
    ) {
        val client = httpClient.toBlocking()
        val response: HttpResponse<*> =
            Assertions.assertDoesNotThrow<HttpResponse<Any?>> {
                client.exchange(
                    "/",
                )
            }
        assertThat(response.status).isEqualTo(HttpStatus.SEE_OTHER)
        assertThat(response.headers[HttpHeaders.LOCATION]).isNotNull()
        assertThat(response.headers[HttpHeaders.LOCATION]).isEqualTo("/docs/redoc")
    }

    @Test
    fun homeControllerIsHidden(
        @Client("/") httpClient: HttpClient,
    ) {
        val client = httpClient.toBlocking()
        val yml =
            Assertions.assertDoesNotThrow<String> {
                client.retrieve(
                    "/swagger/openapi.yaml",
                )
            }
        Assertions.assertFalse(yml.contains("operationId: redirectToDocs"))
    }
}
