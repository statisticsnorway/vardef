package no.ssb.metadata.vardef.integrations.vardok

import io.micronaut.context.annotation.Requires
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.vardok.client.VardokClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@Requires(env = ["integration-test"])
@MicronautTest
class VardokResponseTest {

    @Inject
    lateinit var vardokClient: VardokClient

    @Test
    fun `calculation in response`(){
        val response = vardokClient.fetchVardokById("566")
        assertThat(response.variable?.calculation).isNotNull
    }

    @Test
    fun `calculation not in response`(){
        val response = vardokClient.fetchVardokById("2")
        assertThat(response.variable?.calculation).isEmpty()
    }
}