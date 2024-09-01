package no.ssb.metadata.vardef

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.viascom.nanoid.NanoId
import jakarta.inject.Inject
import no.ssb.metadata.vardef.models.*
import no.ssb.metadata.vardef.services.VariableDefinitionService
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValidityPeriodsTest {
    @Inject
    lateinit var variableDefinitionService: VariableDefinitionService

    val savedVariableDefinition =
        SavedVariableDefinition(
            id = ObjectId(),
            definitionId = NanoId.generate(8),
            patchId = 1,
            name =
                LanguageStringType(
                    nb = "Landbakgrunn",
                    nn = "Landbakgrunn",
                    en = "Country Background",
                ),
            shortName = "landbak",
            definition =
                LanguageStringType(
                    nb = "For personer født",
                    nn = "For personer født",
                    en = "Country background is",
                ),
            classificationUri = "91",
            unitTypes = listOf("01", "02"),
            subjectFields = listOf("he04"),
            containsSensitivePersonalInformation = false,
            variableStatus = VariableStatus.PUBLISHED_EXTERNAL,
            measurementType = "02.01",
            validFrom = LocalDate.of(1960, 1, 1),
            validUntil = LocalDate.of(1980, 11, 30),
            externalReferenceUri = URI("https://example.com/").toURL(),
            relatedVariableDefinitionUris = listOf(),
            owner =
                Owner("", ""),
            contact =
                Contact(
                    LanguageStringType("", "", ""),
                    "me@example.com",
                ),
            createdAt = LocalDateTime.parse("2024-06-11T08:15:19"),
            createdBy =
                Person("", ""),
            lastUpdatedAt = LocalDateTime.parse("2024-06-11T08:15:19"),
            lastUpdatedBy =
                Person("", ""),
        )

    @BeforeEach
    fun setUp() {
        variableDefinitionService.clear()

        // Collection of one variable definition
        variableDefinitionService.save(savedVariableDefinition)
        variableDefinitionService.save(savedVariableDefinition.copy().apply { patchId = 2 })
        variableDefinitionService.save(savedVariableDefinition.copy().apply { patchId = 3 })
        variableDefinitionService.save(
            savedVariableDefinition.copy().apply {
                validFrom = LocalDate.of(1980, 12, 1)
                validUntil = LocalDate.of(2020, 12, 31)
                patchId = 4
            },
        )
        variableDefinitionService.save(
            savedVariableDefinition.copy().apply {
                validFrom = LocalDate.of(1980, 12, 1)
                validUntil = LocalDate.of(2020, 12, 31)
                patchId = 5
            },
        )
        variableDefinitionService.save(
            savedVariableDefinition.copy().apply {
                validFrom = LocalDate.of(1980, 12, 1)
                validUntil = LocalDate.of(2020, 12, 31)
                patchId = 6
            },
        )

        variableDefinitionService.save(
            savedVariableDefinition.copy().apply {
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
        )
    }

    @Test
    fun `get correct validity periods`() {
        val definitionId = savedVariableDefinition.definitionId
        val result = variableDefinitionService.listAllByValidityPeriod(definitionId, LocalDate.now())
        assertThat(result.size).isEqualTo(1)
        assertThat(result[0].validUntil).isNull()
        val result2 = variableDefinitionService.listAllByValidityPeriod(definitionId, LocalDate.of(1990, 1, 1))
        assertThat(result2.size).isEqualTo(4)
        val result3 = variableDefinitionService.listAllByValidityPeriod(definitionId, LocalDate.of(1960, 1, 1), LocalDate.of(1969, 1, 1))
        assertThat(result3.size).isEqualTo(3)
    }
}
