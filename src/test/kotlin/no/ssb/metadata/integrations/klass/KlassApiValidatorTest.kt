package no.ssb.metadata.integrations.klass

import io.micronaut.core.annotation.Introspected
import io.micronaut.data.model.query.QueryModel.In
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import no.ssb.metadata.vardef.integrations.klass.models.ClassificationItem
import no.ssb.metadata.vardef.integrations.klass.validators.KlassCode
import no.ssb.metadata.vardef.integrations.klass.validators.KlassCodeUtil.isValid
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@MicronautTest(startApplication = false)
class KlassApiValidatorTest(private val validator: Validator) {

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


    @Test
    fun `klass code validation`() {
        val classItem = ClassificationItem(null,123,null,null,null)
        validator.validate(classItem)

    }
}
