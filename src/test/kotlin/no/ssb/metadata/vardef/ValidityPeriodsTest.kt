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
import java.time.Period

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValidityPeriodsTest {
    @Inject
    lateinit var variableDefinitionService: VariableDefinitionService

    val saveVariableDefinition =
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

    val inputVariableDefinition =
        InputVariableDefinition(
            id = NanoId.generate(8),
            name =
                LanguageStringType(
                    nb = "Landbakgrunn",
                    nn = "Landbakgrunn",
                    en = "Country Background",
                ),
            shortName = "landbak",
            definition =
                LanguageStringType(
                    nb = "For personer født på torsdag",
                    nn = "For personer født på torsdag",
                    en = "Persons born on thursday",
                ),
            classificationReference = "91",
            unitTypes = listOf("", ""),
            subjectFields = listOf("", ""),
            containsSensitivePersonalInformation = false,
            variableStatus = VariableStatus.PUBLISHED_INTERNAL,
            measurementType = "",
            validFrom = LocalDate.of(2022, 1, 1),
            validUntil = null,
            externalReferenceUri = URI("https://www.example.com").toURL(),
            relatedVariableDefinitionUris = listOf(URI("https://www.example.com").toURL()),
            contact =
                Contact(
                    LanguageStringType("", "", ""),
                    "",
                ),
        )

    val newValidityPeriod =
        InputVariableDefinition(
            id = NanoId.generate(8),
            name =
                LanguageStringType(
                    nb = "Landbakgrunn",
                    nn = "Landbakgrunn",
                    en = "Country Background",
                ),
            shortName = "landbak",
            definition =
                LanguageStringType(
                    nb = "For personer født på torsdag og fredag",
                    nn = "For personer født på torsdag og fredag",
                    en = "Persons born on thursday and friday",
                ),
            classificationReference = "91",
            unitTypes = listOf("", ""),
            subjectFields = listOf("", ""),
            containsSensitivePersonalInformation = false,
            variableStatus = VariableStatus.PUBLISHED_INTERNAL,
            measurementType = "",
            validFrom = LocalDate.of(2024, 9, 2),
            validUntil = null,
            externalReferenceUri = URI("https://www.example.com").toURL(),
            relatedVariableDefinitionUris = listOf(URI("https://www.example.com").toURL()),
            contact =
                Contact(
                    LanguageStringType("", "", ""),
                    "",
                ),
        )

    @BeforeEach
    fun setUp() {
        variableDefinitionService.clear()

        variableDefinitionService.save(saveVariableDefinition)
        variableDefinitionService.save(
            saveVariableDefinition.apply {
                validFrom = LocalDate.of(1980, 12, 1)
                validUntil = null
                definition =
                    LanguageStringType(
                        nb = "For personer født oppe",
                        nn = "For personer født oppe",
                        en = "Persons born upstairs",
                    )
                patchId = 2
            },
        )

        variableDefinitionService.save(
            saveVariableDefinition.apply {
                validUntil = LocalDate.of(2020, 12, 31)
                patchId = 3
            },
        )

        variableDefinitionService.save(
            saveVariableDefinition.apply {
                validFrom = LocalDate.of(2021, 1, 1)
                validUntil = null
                definition =
                    LanguageStringType(
                        nb = "For personer født på siden",
                        nn = "For personer født på siden",
                        en = "Persons born on the side",
                    )
                patchId = 4
            },
        )
        variableDefinitionService.save(
            saveVariableDefinition.apply {
                name =
                    LanguageStringType(
                        nb = "Landbakgrunnen",
                        nn = "Landbakgrunnen",
                        en = "The Country Background",
                    )
                patchId = 5
            },
        )
    }

    @Test
    fun `save new validity period`() {
        val latestPatchId = variableDefinitionService.getLatestPatchById(saveVariableDefinition.definitionId)
        val result = variableDefinitionService.saveNewValidityPeriod(inputVariableDefinition, latestPatchId)
        assertThat(result.patchId).isEqualTo(6)
        assertThat(result.validFrom).isEqualTo(inputVariableDefinition.validFrom)
    }

    @Test
    fun `set last validity period valid until`()  {
        val lastPatch = variableDefinitionService.getLatestPatchById(saveVariableDefinition.definitionId)
        assertThat(lastPatch.validUntil == null)
        val patchesBefore = variableDefinitionService.listAllPatchesById(saveVariableDefinition.definitionId)
        assertThat(patchesBefore.size).isEqualTo(5)
        val resultCloseLastValidityPeriod =
            variableDefinitionService.closeLastValidityPeriod(
                saveVariableDefinition.definitionId,
                newValidityPeriod.validFrom,
            )
        val patchesAfter = variableDefinitionService.listAllPatchesById(saveVariableDefinition.definitionId)
        assertThat(patchesAfter.size).isEqualTo(6)
        assertThat(resultCloseLastValidityPeriod.patchId).isEqualTo(6)
        assertThat(resultCloseLastValidityPeriod.validFrom).isEqualTo(
            variableDefinitionService.getLatestPatchById(
                saveVariableDefinition.definitionId,
            ).validFrom,
        )
        assertThat(resultCloseLastValidityPeriod.validUntil).isEqualTo(
            variableDefinitionService.getLatestPatchById(
                saveVariableDefinition.definitionId,
            ).validUntil,
        )
        assertThat(
            variableDefinitionService.getLatestPatchById(
                saveVariableDefinition.definitionId,
            ).validUntil,
        ).isEqualTo(
            newValidityPeriod.validFrom.minus(Period.ofDays(1)),
        )
    }

    @Test
    fun `close validity period`()  {
        val result =
            variableDefinitionService.closeLastValidityPeriod(
                saveVariableDefinition.definitionId,
                newValidityPeriod.validFrom,
            )
        assertThat(result).isNotNull
        assertThat(result.patchId).isEqualTo(6)
        assertThat(result.validUntil).isNotNull()
        assertThat(result.validUntil).isAfter(
            variableDefinitionService.getOnePatchById(
                saveVariableDefinition.definitionId,
                6,
            ).validFrom,
        )
    }
}
