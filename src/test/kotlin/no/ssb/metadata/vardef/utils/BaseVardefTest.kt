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
import java.time.LocalDate

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
        variableDefinitionService.save(SAVED_VARIABLE_DEFINITION)
        variableDefinitionService.save(SAVED_VARIABLE_DEFINITION_COPY)
        variableDefinitionService.save(SAVED_VARIABLE_DEFINITION.apply { patchId = 2 })
        variableDefinitionService.save(SAVED_VARIABLE_DEFINITION.apply { patchId = 3 })
        variableDefinitionService.save(
            SAVED_VARIABLE_DEFINITION.apply {
                validFrom = LocalDate.of(1980, 12, 1)
                patchId = 4
            },
        )
        variableDefinitionService.save(
            SAVED_VARIABLE_DEFINITION.apply {
                validFrom = LocalDate.of(1980, 12, 1)
                patchId = 5
            },
        )
        variableDefinitionService.save(
            SAVED_VARIABLE_DEFINITION.apply {
                validUntil = LocalDate.of(2030, 12, 31)
                patchId = 6
            },
        )
        variableDefinitionService.save(INPUT_VARIABLE_DEFINITION.toSavedVariableDefinition())
        variableDefinitionService.save(INPUT_VARIABLE_DEFINITION_COPY.toSavedVariableDefinition())
        variableDefinitionService.save(INPUT_VARIABLE_DEFINITION_NO_NAME.toSavedVariableDefinition())
        variableDefinitionService.save(SAVED_DRAFT_VARIABLE_DEFINITION)

    }
}
