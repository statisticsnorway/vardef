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

    @ParameterizedTest
    @MethodSource("listAllVariablesAtDifferentTimes")
    fun `list variable definitions at different times`(
        date: LocalDate?,
        result: Int,
    ) {
        val variableDefinitions =
            variableDefinitionService
                .listCompleteForDate(
                    date,
                    null,
                )

        assertThat(variableDefinitions.size).isEqualTo(result)
    }

    @ParameterizedTest
    @MethodSource("variableByShortNameCases")
    fun `list variable definition by shortName`(
        date: LocalDate?,
        patchId: Int?,
        shortName: String?,
    ) {
        assertThat(variableDefinitionService.listCompleteForDate(date, shortName).first().patchId)
            .isEqualTo(patchId)
    }

    @Test
    fun `is generated contact`() {
        assertThat(
            variableDefinitionService.isIllegalContactForPublishing(
                SAVED_TO_PUBLISH_ILLEGAL_CONTACT,
                UpdateDraft(variableStatus = VariableStatus.PUBLISHED_EXTERNAL),
            ),
        ).isTrue()
    }

    @ParameterizedTest
    @MethodSource("variableStatusTestCases")
    fun `get by variable status`(
        definitionId: String,
        status: VariableStatus?,
        expectedResult: CompleteView?,
    ) {
        assertThat(
            variableDefinitionService
                .getCompleteByDateAndStatus(definitionId, null, status),
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

    @ParameterizedTest
    @MethodSource("languagesForExternalPublicationWithUpdateDraft")
    fun `languages for external publication (UpdateDraft)`(
        updates: UpdateDraft,
        existingVariable: SavedVariableDefinition,
        expected: Boolean,
    ) {
        assertThat(
            variableDefinitionService.allLanguagesPresentForExternalPublication(updates, existingVariable),
        ).isEqualTo(expected)
    }

    @ParameterizedTest
    @MethodSource("languagesForExternalPublicationWithPatch")
    fun `languages for external publication (Patch)`(
        updates: Patch,
        existingVariable: SavedVariableDefinition,
        expected: Boolean,
    ) {
        assertThat(
            variableDefinitionService.allLanguagesPresentForExternalPublication(updates, existingVariable),
        ).isEqualTo(expected)
    }

    companion object {
        private val allLanguagesPresent =
            LanguageStringType(
                nb = "Norwegian Bokmål",
                nn = "Norwegian Nynorsk",
                en = "English",
            )
        private val allLanguagesNull =
            LanguageStringType(
                nb = null,
                nn = null,
                en = null,
            )
        private val allLanguagesEmpty =
            LanguageStringType(
                nb = "",
                nn = "",
                en = "",
            )

        @JvmStatic
        fun languagesForExternalPublicationWithPatch(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "All languages present",
                    Patch(
                        variableStatus = VariableStatus.PUBLISHED_EXTERNAL,
                    ),
                    SAVED_INTERNAL_VARIABLE_DEFINITION.copy(
                        name = allLanguagesPresent,
                        definition = allLanguagesPresent,
                        comment = allLanguagesPresent,
                    ),
                    true,
                ),
                argumentSet(
                    "One field all languages empty",
                    Patch(
                        variableStatus = VariableStatus.PUBLISHED_EXTERNAL,
                    ),
                    SAVED_INTERNAL_VARIABLE_DEFINITION.copy(
                        name = allLanguagesEmpty,
                        definition = allLanguagesPresent,
                        comment = allLanguagesPresent,
                    ),
                    false,
                ),
            )

        @JvmStatic
        fun languagesForExternalPublicationWithUpdateDraft(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "All languages present",
                    UpdateDraft(
                        variableStatus = VariableStatus.PUBLISHED_EXTERNAL,
                    ),
                    SAVED_INTERNAL_VARIABLE_DEFINITION.copy(
                        name = allLanguagesPresent,
                        definition = allLanguagesPresent,
                        comment = allLanguagesPresent,
                    ),
                    true,
                ),
                argumentSet(
                    "One field all languages empty",
                    UpdateDraft(
                        variableStatus = VariableStatus.PUBLISHED_EXTERNAL,
                    ),
                    SAVED_INTERNAL_VARIABLE_DEFINITION.copy(
                        name = allLanguagesEmpty,
                        definition = allLanguagesPresent,
                        comment = allLanguagesPresent,
                    ),
                    false,
                ),
                argumentSet(
                    "Variable status not PUBLISHED_EXTERNAL",
                    UpdateDraft(
                        variableStatus = VariableStatus.DRAFT,
                    ),
                    SAVED_INTERNAL_VARIABLE_DEFINITION.copy(
                        name = allLanguagesNull,
                        definition = allLanguagesPresent,
                        comment = allLanguagesPresent,
                    ),
                    true,
                ),
                argumentSet(
                    "Update includes languages set to null",
                    UpdateDraft(
                        variableStatus = VariableStatus.PUBLISHED_EXTERNAL,
                        name = allLanguagesNull,
                    ),
                    SAVED_INTERNAL_VARIABLE_DEFINITION.copy(
                        name = allLanguagesPresent,
                        definition = allLanguagesPresent,
                        comment = allLanguagesPresent,
                    ),
                    false,
                ),
                argumentSet(
                    "Update attempts to fill languages set to null",
                    UpdateDraft(
                        variableStatus = VariableStatus.PUBLISHED_EXTERNAL,
                        name = allLanguagesPresent,
                    ),
                    SAVED_INTERNAL_VARIABLE_DEFINITION.copy(
                        name = allLanguagesNull,
                        definition = allLanguagesPresent,
                        comment = allLanguagesPresent,
                    ),
                    false,
                ),
                argumentSet(
                    "Optional field set to null",
                    UpdateDraft(
                        variableStatus = VariableStatus.PUBLISHED_EXTERNAL,
                    ),
                    SAVED_INTERNAL_VARIABLE_DEFINITION.copy(
                        name = allLanguagesPresent,
                        definition = allLanguagesPresent,
                        comment = null,
                    ),
                    true,
                ),
            )

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
                    INCOME_TAX_VP2_P6.toCompleteView(),
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
                    DRAFT_BUS_EXAMPLE.toCompleteView(),
                ),
                argumentSet(
                    "Published Internal matching",
                    SAVED_INTERNAL_VARIABLE_DEFINITION.definitionId,
                    VariableStatus.PUBLISHED_INTERNAL,
                    SAVED_INTERNAL_VARIABLE_DEFINITION.toCompleteView(),
                ),
            )

        @JvmStatic
        fun variableByShortNameCases(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "Fist validity period",
                    LocalDate.of(1980, 1, 1),
                    7,
                    INCOME_TAX_VP1_P1.shortName,
                ),
                argumentSet(
                    "Second validity period",
                    LocalDate.of(2021, 1, 1),
                    6,
                    INCOME_TAX_VP2_P6.shortName,
                ),
                argumentSet(
                    "No date",
                    null,
                    6,
                    INCOME_TAX_VP2_P6.shortName,
                ),
            )

        @JvmStatic
        fun listAllVariablesAtDifferentTimes(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "All variables",
                    LocalDate.now(),
                    NUM_ALL_VARIABLE_DEFINITIONS,
                ),
                argumentSet(
                    "Variables from year 100",
                    LocalDate.of(100, 1, 1),
                    0,
                ),
                argumentSet(
                    "No date input",
                    null,
                    NUM_ALL_VARIABLE_DEFINITIONS,
                ),
            )
    }
}
