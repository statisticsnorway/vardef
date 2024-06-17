package no.ssb.metadata

import io.micronaut.json.JsonMapper
import jakarta.inject.Inject
import no.ssb.metadata.constants.INPUT_VARIABLE_DEFINITION_EXAMPLE
import no.ssb.metadata.constants.RENDERED_VARIABLE_DEFINITION
import no.ssb.metadata.constants.UPDATE_VARIABLE_DEFINITION
import no.ssb.metadata.models.InputVariableDefinition
import no.ssb.metadata.models.RenderedVariableDefinition
import no.ssb.metadata.models.UpdateVariableDefinition
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ConstantsTest {
    @Inject
    lateinit var jsonMapper: JsonMapper

    @ParameterizedTest
    @ValueSource(strings = [RENDERED_VARIABLE_DEFINITION, INPUT_VARIABLE_DEFINITION_EXAMPLE, UPDATE_VARIABLE_DEFINITION])
    fun `examples are valid json`(string: String) {
        Assertions.assertNotNull(JSONObject(string))
    }

    @Disabled("TODO: Complete under DPMETA-257")
    @Test
    fun `rendered variable definition example is valid`() {
        Assertions.assertNotNull(
            jsonMapper.readValue(RENDERED_VARIABLE_DEFINITION, RenderedVariableDefinition::class.java),
        )
    }

    @Disabled("TODO: Complete under DPMETA-257")
    @Test
    fun `input variable definition example is valid`() {
        Assertions.assertNotNull(
            jsonMapper.readValue(INPUT_VARIABLE_DEFINITION_EXAMPLE, InputVariableDefinition::class.java),
        )
    }

    @Disabled("TODO: Complete under DPMETA-257")
    @Test
    fun `update variable definition example is valid`() {
        Assertions.assertNotNull(
            jsonMapper.readValue(UPDATE_VARIABLE_DEFINITION, UpdateVariableDefinition::class.java),
        )
    }
}
