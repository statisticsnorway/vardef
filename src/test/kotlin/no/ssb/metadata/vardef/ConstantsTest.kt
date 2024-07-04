package no.ssb.metadata.vardef

import io.micronaut.json.JsonMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.constants.INPUT_VARIABLE_DEFINITION_EXAMPLE
import no.ssb.metadata.vardef.constants.RENDERED_VARIABLE_DEFINITION_EXAMPLE
import no.ssb.metadata.vardef.models.InputVariableDefinition
import no.ssb.metadata.vardef.models.RenderedVariableDefinition
import no.ssb.metadata.vardef.models.UpdateVariableDefinition
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@MicronautTest(startApplication = false)
class ConstantsTest {
    @Inject
    lateinit var jsonMapper: JsonMapper

    @ParameterizedTest
    @ValueSource(
        strings = [
            RENDERED_VARIABLE_DEFINITION_EXAMPLE,
            INPUT_VARIABLE_DEFINITION_EXAMPLE,
        ],
    )
    fun `examples are valid json`(string: String) {
        Assertions.assertNotNull(JSONObject(string))
    }

    @Test
    fun `rendered variable definition example is valid`() {
        Assertions.assertNotNull(
            jsonMapper.readValue(
                RENDERED_VARIABLE_DEFINITION_EXAMPLE,
                RenderedVariableDefinition::class.java,
            ),
        )
    }

    @Test
    fun `input variable definition example is valid`() {
        Assertions.assertNotNull(
            jsonMapper.readValue(
                INPUT_VARIABLE_DEFINITION_EXAMPLE,
                InputVariableDefinition::class.java,
            ),
        )
    }

    @Test
    fun `input variable definition example is valid for update`() {
        Assertions.assertNotNull(
            jsonMapper.readValue(
                INPUT_VARIABLE_DEFINITION_EXAMPLE,
                UpdateVariableDefinition::class.java,
            ),
        )
    }
}
