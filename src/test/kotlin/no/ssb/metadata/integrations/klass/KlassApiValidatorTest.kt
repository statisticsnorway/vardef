package no.ssb.metadata.integrations.klass

import no.ssb.metadata.vardef.integrations.klass.validators.KlassCodeUtil.isValid
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class KlassApiValidatorTest {
    @ParameterizedTest
    @ValueSource(
        ints = [
            13, 22, 7, 33, 8,
        ],
    )
    fun `invalid klass codes`(code: Int) {
        assertThat(isValid(code)).isFalse()
    }

    @ParameterizedTest
    @ValueSource(
        ints = [
            131, 17, 68,
        ],
    )
    fun `valid klass codes`(code: Int) {
        assertThat(isValid(code)).isTrue()
    }
}
