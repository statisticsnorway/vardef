package no.ssb.metadata.vardef.constants

import io.micronaut.json.JsonMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.models.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@MicronautTest(startApplication = false)
class SchemaExamplesTest {
    @Inject
    lateinit var jsonMapper: JsonMapper

    companion object {
        @JvmStatic
        fun schemaExamples(): Stream<Arguments.ArgumentSet> =
            Stream.of(
                argumentSet(
                    "KlassReference example validates",
                    KLASS_REFERENCE_SUBJECT_FIELD_EXAMPLE,
                    listOf(KlassReference::class.java),
                ),
                argumentSet(
                    "Owner example validates",
                    OWNER_EXAMPLE,
                    listOf(Owner::class.java),
                ),
                argumentSet(
                    "Draft example validates",
                    DRAFT_UPDATE_EXAMPLE,
                    listOf(Draft::class.java, UpdateDraft::class.java),
                ),
                argumentSet(
                    "CompleteResponse example validates",
                    COMPLETE_RESPONSE_EXAMPLE,
                    listOf(CompleteResponse::class.java),
                ),
                argumentSet("Patch example validates", PATCH_EXAMPLE, listOf(Patch::class.java)),
                argumentSet(
                    "ValidityPeriod example validates",
                    VALIDITY_PERIOD_EXAMPLE,
                    listOf(ValidityPeriod::class.java),
                ),
                argumentSet(
                    "RenderedVariableDefinition example validates",
                    RENDERED_VARIABLE_DEFINITION_EXAMPLE,
                    listOf(RenderedVariableDefinition::class.java),
                ),
            )
    }

    @ParameterizedTest
    @MethodSource("schemaExamples")
    fun `OpenAPI schema examples deserialise to data classes`(
        example: String,
        dataClasses: List<Class<*>>,
    ) {
        dataClasses.forEach {
            Assertions.assertNotNull(
                jsonMapper.readValue(
                    example,
                    it,
                ),
            )
        }
    }
}
