package no.ssb.metadata.vardef.utils

import INPUT_VARIABLE_DEFINITION
import INPUT_VARIABLE_DEFINITION_COPY
import INPUT_VARIABLE_DEFINITION_NO_NAME
import SAVED_VARIABLE_DEFINITION
import SAVED_VARIABLE_DEFINITION_COPY
import io.micronaut.context.annotation.Property
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

    @Property(name = "micronaut.http.services.klass.url")
    private var klassUrl: String = ""

    @BeforeEach
    fun setUp() {
        variableDefinitionService.clear()
        variableDefinitionService.save(SAVED_VARIABLE_DEFINITION)
        variableDefinitionService.save(SAVED_VARIABLE_DEFINITION_COPY)
        variableDefinitionService.save(INPUT_VARIABLE_DEFINITION.toSavedVariableDefinition())
        variableDefinitionService.save(INPUT_VARIABLE_DEFINITION_COPY.toSavedVariableDefinition())
        variableDefinitionService.save(INPUT_VARIABLE_DEFINITION_NO_NAME.toSavedVariableDefinition())
    }
}
