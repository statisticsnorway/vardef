package no.ssb.metadata

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VardefControllerTest {

    @Test
    fun index() {
        val vardefcontroller = VardefController()
        val result = vardefcontroller.index()
        assertThat(result).isEqualTo("Example Response")
    }
}