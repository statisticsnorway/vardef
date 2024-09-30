package no.ssb.metadata.vardef.utils

import io.micronaut.json.JsonMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.RestAssured
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import jakarta.inject.Inject
import no.ssb.metadata.vardef.models.LanguageStringType
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

        // One variable definition with many periods and patches
        variableDefinitionService.save(SAVED_TAX_EXAMPLE)
        variableDefinitionService.save(
            SAVED_TAX_EXAMPLE.copy(
                unitTypes = listOf("01", "02", "03"),
                patchId = 2,
            ),
        )

        variableDefinitionService.save(
            SAVED_TAX_EXAMPLE.copy(
                patchId = 3,
                unitTypes = listOf("01", "02", "03", "04"),
            ),
        )
        variableDefinitionService.save(
            SAVED_TAX_EXAMPLE.copy(
                patchId = 4,
                validUntil = LocalDate.of(2020, 12, 31),
            ),
        )

        variableDefinitionService.save(
            SAVED_TAX_EXAMPLE.copy(
                validFrom = LocalDate.of(2021, 1, 1),
                definition =
                    LanguageStringType(
                        "Intektsskatt ny definisjon",
                        "Intektsskatt ny definisjon",
                        "Income tax new definition",
                    ),
                patchId = 5,
            ),
        )

        variableDefinitionService.save(
            SAVED_TAX_EXAMPLE.copy(
                unitTypes = listOf("01", "02"),
                patchId = 6,
                validFrom = LocalDate.of(2021, 1, 1),
                validUntil = null,
                definition =
                LanguageStringType(
                    "Intektsskatt ny definisjon",
                    "Intektsskatt ny definisjon",
                    "Income tax new definition",
                ),
            ),
        )

        // One variable definition
        variableDefinitionService.save(DRAFT_BUS_EXAMPLE.toSavedVariableDefinition())

        // One variable definition
        variableDefinitionService.save(SAVED_DRAFT_DEADWEIGHT_EXAMPLE)
    }
}
