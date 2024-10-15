package no.ssb.metadata.vardef.utils

import io.micronaut.json.JsonMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.RestAssured
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import jakarta.inject.Inject
import no.ssb.metadata.vardef.services.PatchesService
import no.ssb.metadata.vardef.services.ValidityPeriodsService
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
    lateinit var validityPeriods: ValidityPeriodsService

    @Inject
    lateinit var patches: PatchesService

    @Inject
    lateinit var jsonMapper: JsonMapper

    @BeforeEach
    fun setUp() {
        variableDefinitionService.clear()

        ALL_INCOME_TAX_PATCHES.map { variableDefinitionService.save(it) }

        // One variable definition
        variableDefinitionService.save(DRAFT_BUS_EXAMPLE.toSavedVariableDefinition())

        // One variable definition
        variableDefinitionService.save(SAVED_DRAFT_DEADWEIGHT_EXAMPLE)

        // One variable definition
        variableDefinitionService.save(SAVED_DEPRECATED_VARIABLE_DEFINITION)
    }
}
