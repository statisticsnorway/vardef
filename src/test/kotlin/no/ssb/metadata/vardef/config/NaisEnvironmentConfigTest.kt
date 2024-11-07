package no.ssb.metadata.vardef.config

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.config.NAIS_CLUSTER_NAME
import no.ssb.metadata.config.NaisEnvironment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@Property(name = NAIS_CLUSTER_NAME, value = "nais-prod")
@MicronautTest
class NaisEnvironmentConfigTest {
    @Inject
    lateinit var applicationContext: ApplicationContext

    @Test
    fun `naisprod environment detected`() {
        assertThat(applicationContext.environment.activeNames).contains(NaisEnvironment.PROD.envName)
    }
}
