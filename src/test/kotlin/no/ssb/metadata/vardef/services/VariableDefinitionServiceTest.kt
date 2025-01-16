package no.ssb.metadata.vardef.services

import no.ssb.metadata.vardef.exceptions.InvalidOwnerStructureError
import no.ssb.metadata.vardef.models.*
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.util.stream.Stream

class VariableDefinitionServiceTest : BaseVardefTest() {
    @Test
    fun `list public variable definitions`() {
        variableDefinitionService
            .listPublicForDate(
                SupportedLanguages.EN,
                LocalDate.now(),
            ).let {
                assertThat(it.size).isEqualTo(
                    NUM_PUBLISHED_EXTERNAL_VARIABLE_DEFINITIONS,
                )
            }
    }

    @Test
    fun `list all variable definitions`() {
        variableDefinitionService
            .listCompleteForDate(
                LocalDate.now(),
            ).let {
                assertThat(it.size).isEqualTo(
                    NUM_ALL_VARIABLE_DEFINITIONS,
                )
            }
    }

    @ParameterizedTest
    @MethodSource("variableStatusTestCases")
    fun `get by variable status`(
        definitionId: String,
        status: VariableStatus?,
        expectedResult: CompleteResponse?,
    ) {
        assertThat(
            variableDefinitionService
                .getCompleteByDate(definitionId, null, status),
        ).usingRecursiveComparison()
            .ignoringFields("createdAt", "lastUpdatedAt")
            .isEqualTo(expectedResult)
    }

    @ParameterizedTest
    @CsvSource(
        "intskatt, true",
        "nyttkortnavn, false",
    )
    fun `check if short name is valid`(
        shortName: String,
        expectedResult: Boolean,
    ) {
        assertThat(variableDefinitionService.doesShortNameExist(shortName)).isEqualTo(expectedResult)
    }

    @ParameterizedTest
    @MethodSource("validOwnerUpdate")
    fun `update owner valid`(
        updateDraft: UpdateDraft,
        valueBefore: Owner,
    ) {
        val updatedSavedVariable = variableDefinitionService.update(SAVED_DRAFT_DEADWEIGHT_EXAMPLE, updateDraft, TEST_USER)
        assertThat(updatedSavedVariable.owner).isNotEqualTo(valueBefore)
    }

    @ParameterizedTest
    @MethodSource("invalidOwnerUpdate")
    fun `update owner invalid`(
        updateDraft: UpdateDraft,
        valueBefore: Owner,
    ) {
        assertThrows<InvalidOwnerStructureError> {
            variableDefinitionService.update(
                SAVED_DRAFT_DEADWEIGHT_EXAMPLE,
                updateDraft,
                TEST_USER,
            )
        }
        assertThat(SAVED_DRAFT_DEADWEIGHT_EXAMPLE.owner).isEqualTo(valueBefore)
    }

    @Test
    fun `correct date order`() {
        val updateFrom = UpdateDraft(unitTypes = listOf("03"))
        //      validFrom = LocalDate.of(2021, 1, 1),
        //        validUntil = LocalDate.of(2030, 9, 15),
        val updateValidUntilValid = UpdateDraft(validUntil = LocalDate.of(2022, 1, 1))
        val updateValidUntilInvalid = UpdateDraft(validUntil = LocalDate.of(2020, 1, 1))
        val updateValidFromValid = UpdateDraft(validFrom = LocalDate.of(2022, 1, 1))
        val updateValidFromInvalid = UpdateDraft(validFrom = LocalDate.of(2040, 1, 1))
        val updateBothValid = UpdateDraft(validFrom = LocalDate.of(2030, 1, 1), validUntil = LocalDate.of(2040, 1, 1))
        val updateBothInvalid = UpdateDraft(validFrom = LocalDate.of(2026, 2, 6), validUntil = LocalDate.of(2023, 1, 1))
        val resultInvalid = variableDefinitionService.isCorrectComparedToSaved(updateValidUntilInvalid, DRAFT_EXAMPLE_WITH_VALID_UNTIL)
        val resultValid = variableDefinitionService.isCorrectComparedToSaved(updateValidUntilValid, DRAFT_EXAMPLE_WITH_VALID_UNTIL)
        val resultInvalidFrom = variableDefinitionService.isCorrectComparedToSaved(updateValidFromInvalid, DRAFT_EXAMPLE_WITH_VALID_UNTIL)
        val resultValidFrom = variableDefinitionService.isCorrectComparedToSaved(updateValidFromValid, DRAFT_EXAMPLE_WITH_VALID_UNTIL)
        val resultValidBoth = variableDefinitionService.isCorrectComparedToSaved(updateBothValid, DRAFT_EXAMPLE_WITH_VALID_UNTIL)
        val resultInvalidBoth = variableDefinitionService.isCorrectComparedToSaved(updateBothInvalid, DRAFT_EXAMPLE_WITH_VALID_UNTIL)
        assertThat(resultInvalid).isFalse()
        assertThat(resultValid).isTrue()
        assertThat(resultValidFrom).isTrue()
        assertThat(resultInvalidFrom).isFalse()
        assertThat(resultValidBoth).isTrue()
    }

    companion object {
        @JvmStatic
        fun invalidOwnerUpdate(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "New team",
                    UpdateDraft(
                        owner =
                            Owner(
                                "my-team",
                                listOf(
                                    "skip-stat-developers",
                                    TEST_DEVELOPERS_GROUP,
                                ),
                            ),
                    ),
                    SAVED_DRAFT_DEADWEIGHT_EXAMPLE.owner,
                ),
                argumentSet(
                    "Remove group associated with team",
                    UpdateDraft(
                        owner =
                            Owner(
                                "skip-stat",
                                listOf(
                                    TEST_DEVELOPERS_GROUP,
                                ),
                            ),
                    ),
                    SAVED_DRAFT_DEADWEIGHT_EXAMPLE.owner,
                ),
            )

        @JvmStatic
        fun validOwnerUpdate(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "Update team and add group",
                    UpdateDraft(
                        owner =
                            Owner(
                                "dapla-felles",
                                listOf(
                                    "pers-skatt-developers",
                                    TEST_DEVELOPERS_GROUP,
                                    "neighbourhood-dogs",
                                    "dapla-felles-developers",
                                ),
                            ),
                    ),
                    SAVED_DRAFT_DEADWEIGHT_EXAMPLE.owner,
                ),
                argumentSet(
                    "Update team",
                    UpdateDraft(
                        owner =
                            Owner(
                                TEST_TEAM,
                                listOf(
                                    "skip-stat-developers",
                                    TEST_DEVELOPERS_GROUP,
                                ),
                            ),
                    ),
                    SAVED_DRAFT_DEADWEIGHT_EXAMPLE.owner,
                ),
                argumentSet(
                    "Add group",
                    UpdateDraft(
                        owner =
                            Owner(
                                TEST_TEAM,
                                listOf(
                                    "skip-stat-developers",
                                    TEST_DEVELOPERS_GROUP,
                                    "dapla-felles-developers",
                                ),
                            ),
                    ),
                    SAVED_DRAFT_DEADWEIGHT_EXAMPLE.owner,
                ),
                argumentSet(
                    "Remove group",
                    UpdateDraft(
                        owner =
                            Owner(
                                "skip-stat",
                                listOf(
                                    "skip-stat-developers",
                                ),
                            ),
                    ),
                    SAVED_DRAFT_DEADWEIGHT_EXAMPLE.owner,
                ),
            )

        @JvmStatic
        fun variableStatusTestCases(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "Published External matching",
                    INCOME_TAX_VP2_P6.definitionId,
                    VariableStatus.PUBLISHED_EXTERNAL,
                    INCOME_TAX_VP2_P6.toCompleteResponse(),
                ),
                argumentSet(
                    "Published External on Draft",
                    DRAFT_BUS_EXAMPLE.definitionId,
                    VariableStatus.PUBLISHED_EXTERNAL,
                    null,
                ),
                argumentSet(
                    "Published External on Published Internal",
                    SAVED_INTERNAL_VARIABLE_DEFINITION.definitionId,
                    VariableStatus.PUBLISHED_EXTERNAL,
                    null,
                ),
                argumentSet(
                    "Draft matching",
                    DRAFT_BUS_EXAMPLE.definitionId,
                    VariableStatus.DRAFT,
                    DRAFT_BUS_EXAMPLE.toCompleteResponse(),
                ),
                argumentSet(
                    "Published Internal matching",
                    SAVED_INTERNAL_VARIABLE_DEFINITION.definitionId,
                    VariableStatus.PUBLISHED_INTERNAL,
                    SAVED_INTERNAL_VARIABLE_DEFINITION.toCompleteResponse(),
                ),
            )
    }
}
