package no.ssb.metadata.vardef.utils

import io.micronaut.json.JsonMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.RestAssured
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import jakarta.inject.Inject
import no.ssb.metadata.vardef.services.VariableDefinitionService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class BaseVardefTest {
    init {
        RestAssured.filters(RequestLoggingFilter(), ResponseLoggingFilter())
    }

    @Inject
    lateinit var variableDefinitionService: VariableDefinitionService

    @Inject
    lateinit var jsonMapper: JsonMapper

    @BeforeEach
    fun setUp() {
        variableDefinitionService.clear()

        // Collection of one variable definition
        variableDefinitionService.save(SAVED_VARIABLE_DEFINITION)
        /*variableDefinitionService.save(SAVED_VARIABLE_DEFINITION.copy().apply { patchId = 2 })
        variableDefinitionService.save(SAVED_VARIABLE_DEFINITION.copy().apply { patchId = 3 })
        variableDefinitionService.save(
            SAVED_VARIABLE_DEFINITION.copy().apply {
                validFrom = LocalDate.of(1980, 12, 1)
                validUntil = LocalDate.of(2020, 12, 31)
                patchId = 4
            },
        )
        variableDefinitionService.save(
            SAVED_VARIABLE_DEFINITION.copy().apply {
                validFrom = LocalDate.of(1980, 12, 1)
                validUntil = LocalDate.of(2020, 12, 31)
                patchId = 5
            },
        )
        variableDefinitionService.save(
            SAVED_VARIABLE_DEFINITION.copy().apply {
                validFrom = LocalDate.of(1980, 12, 1)
                validUntil = LocalDate.of(2020, 12, 31)
                patchId = 6
            },
        )

        variableDefinitionService.save(
            SAVED_VARIABLE_DEFINITION.copy().apply {
                validFrom = LocalDate.of(2021, 1, 1)
                validUntil = null
                definition =
                    LanguageStringType(
                        nb = "For personer født på siden",
                        nn = "For personer født på siden",
                        en = "Persons born on the side",
                    )
                patchId = 7
            },
        )*/

        // Collection of one variable definition
        variableDefinitionService.save(INPUT_VARIABLE_DEFINITION.toSavedVariableDefinition(null))
        variableDefinitionService.save(INPUT_VARIABLE_DEFINITION_COPY.toSavedVariableDefinition(null))
        variableDefinitionService.save(INPUT_VARIABLE_DEFINITION_NO_NAME.toSavedVariableDefinition(null))

        // Collection of one variable definition
        variableDefinitionService.save(SAVED_VARIABLE_DEFINITION_COPY)

        // Collection of one variable definition
        variableDefinitionService.save(SAVED_DRAFT_VARIABLE_DEFINITION)
    }
}
