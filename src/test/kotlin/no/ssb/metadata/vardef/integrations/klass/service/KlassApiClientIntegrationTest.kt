package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.klass.models.Code
import no.ssb.metadata.vardef.models.SupportedLanguages
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@Requires(env = ["integration-test"])
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KlassApiClientIntegrationTest {
    @Inject
    lateinit var klassApiClient: KlassApiClient

    @Property(name = "klass.cached-classifications.unit-types")
    private val unitTypesId: Int = 702

    @Property(name = "klass.cached-classifications.areas")
    private val areasId: Int = 618

    @Property(name = "klass.codes-at")
    private val codesAt: String = "2024-08-01"

    @Test
    fun `fetch code list from klass api`() {
        listOf(unitTypesId, areasId)
            .forEach { id ->
                val result = klassApiClient.listCodes(id, codesAt, language = SupportedLanguages.NB)
                assertThat(result).isNotNull

                val classificationList = result.body()?.codes ?: emptyList()
                assertThat(classificationList[0]).isInstanceOf(Code::class.java)
                assertThat(classificationList.size > 1)
            }
    }

    @Test
    fun `fetch classification from klass api`() {
        listOf(unitTypesId, areasId)
            .forEach { id ->
                val result = klassApiClient.fetchClassification(id)
                assertThat(result).isNotNull
                assertThat(id == result.body()?.id)
            }
    }
}
