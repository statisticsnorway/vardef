package no.ssb.metadata.vardef

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.models.*
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.utils.INPUT_VALIDITY_PERIOD
import no.ssb.metadata.vardef.utils.SAVED_VARIABLE_DEFINITION
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate
import java.time.Period

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValidityPeriodsTest {
    @Inject
    lateinit var variableDefinitionService: VariableDefinitionService

    private val saveVariableDefinition =
        SAVED_VARIABLE_DEFINITION.copy(
            validFrom = LocalDate.of(1960, 1, 1),
            validUntil = LocalDate.of(1980, 11, 30),
        )

    private val newValidityPeriod =
        INPUT_VALIDITY_PERIOD.copy(
            definition =
                LanguageStringType(
                    nb = "For personer født på torsdag og fredag",
                    nn = "For personer født på torsdag og fredag",
                    en = "Persons born on thursday and friday",
                ),
            validFrom = LocalDate.of(2024, 9, 2),
            validUntil = null,
        )

    private val newValidityPeriodPreFirstPeriod =
        newValidityPeriod.copy(
            definition =
                LanguageStringType(
                    nb = "For personer født på lørdag",
                    nn = "For personer født på lørdag",
                    en = "Persons born on Saturday",
                ),
            validFrom = LocalDate.of(1796, 1, 1),
            validUntil = null,
        )

    private val newValidityPeriodFuture =
        newValidityPeriod.copy(
            definition =
                LanguageStringType(
                    nb = "For personer født på baksiden",
                    nn = "For personer født på baksiden",
                    en = "Persons born on the backside",
                ),
            validFrom = LocalDate.of(2050, 1, 1),
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
    fun `end validity period`() {
        val patchValidUntil =
            variableDefinitionService.endLastValidityPeriod(
                saveVariableDefinition.definitionId,
                newValidityPeriod.validFrom,
            )

        assertThat(patchValidUntil.validUntil).isAfter(patchValidUntil.validFrom)

        val expectedPatchId =
            variableDefinitionService.getLatestPatchById(
                saveVariableDefinition.definitionId,
            ).patchId
        val expectedValidUntil = newValidityPeriod.validFrom.minus(Period.ofDays(1))

        assertThat(patchValidUntil.patchId).isEqualTo(expectedPatchId)
        assertThat(patchValidUntil.validUntil).isEqualTo(expectedValidUntil)
    }

    @Test
    fun `save new validity period after last validity period`() {
        val patchesBeforeNewValidityPeriod =
            variableDefinitionService.listAllPatchesById(
                saveVariableDefinition.definitionId,
            )
        assertThat(patchesBeforeNewValidityPeriod.size).isEqualTo(5)

        val latestExistingPatch =
            variableDefinitionService.getLatestPatchById(
                saveVariableDefinition.definitionId,
            )
        assertThat(latestExistingPatch.validUntil).isNull()

        val saveNewValidityPeriod =
            variableDefinitionService.saveNewValidityPeriod(
                newValidityPeriod,
                saveVariableDefinition.definitionId,
            )

        val patchesAfterNewValidityPeriod =
            variableDefinitionService.listAllPatchesById(
                saveVariableDefinition.definitionId,
            )
        assertThat(patchesAfterNewValidityPeriod.size).isEqualTo(7)

        val expectedPatchId = patchesAfterNewValidityPeriod.last().patchId
        assertThat(saveNewValidityPeriod.patchId).isEqualTo(expectedPatchId)

        // assertThat(saveNewValidityPeriod.validUntil).isNull()
        assertThat(saveNewValidityPeriod.validFrom).isEqualTo(newValidityPeriod.validFrom)
        assertThat(patchesAfterNewValidityPeriod.last().validFrom).isEqualTo(newValidityPeriod.validFrom)

        assertThat(saveNewValidityPeriod.definitionId).isEqualTo(saveVariableDefinition.definitionId)
    }

    @Test
    fun `save new validity period before first validity period`() {
        val saveNewValidityPeriod =
            variableDefinitionService.saveNewValidityPeriod(
                newValidityPeriodPreFirstPeriod,
                saveVariableDefinition.definitionId,
            )
        assertThat(saveNewValidityPeriod).isNotNull

        val patches = variableDefinitionService.listAllPatchesById(saveVariableDefinition.definitionId)
        assertThat(saveNewValidityPeriod.validFrom).isBefore(patches.first().validFrom)
    }

    @Test
    fun `save new validity period in the future`() {
        val saveNewValidityPeriod =
            variableDefinitionService.saveNewValidityPeriod(
                newValidityPeriodFuture,
                saveVariableDefinition.definitionId,
            )
        val patches = variableDefinitionService.listAllPatchesById(saveVariableDefinition.definitionId)
        assertThat(saveNewValidityPeriod).isNotNull
        assertThat(patches[patches.size - 2].validUntil).isEqualTo(
            saveNewValidityPeriod.validFrom.minus(Period.ofDays(1)),
        )
    }

    @Test
    fun `save new validity period before all valid from`() {
        val saveNewValidityPeriod =
            variableDefinitionService.saveNewValidityPeriod(
                newValidityPeriodPreFirstPeriod,
                saveVariableDefinition.definitionId,
            )
        val patches = variableDefinitionService.listAllPatchesById(saveVariableDefinition.definitionId)
        assertThat(saveNewValidityPeriod.patchId).isEqualTo(6)
        assertThat(saveNewValidityPeriod.validUntil).isNotNull()
        saveNewValidityPeriod.validUntil?.let { assertThat(it.isBefore(patches.first().validFrom)) }
    }
}
