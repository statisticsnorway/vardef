package no.ssb.metadata.integrations.klass

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import org.junit.jupiter.api.Test

@MicronautTest
class KlassServiceTest {
    @Inject
    lateinit var klassService: KlassService

    @Test
    fun `get codes`() {
        println(klassService.getCodesFor("702"))
    }
}
