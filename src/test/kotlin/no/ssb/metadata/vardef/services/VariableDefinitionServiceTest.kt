package no.ssb.metadata.vardef.services

import no.ssb.metadata.vardef.constants.ILLEGAL_SHORTNAME_KEYWORD
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
    fun `check mandatory fields before publish`(){
        assertThat(variableDefinitionService.hasRequiredFieldsForPublishing(
            SAVED_TO_PUBLISH, UpdateDraft(variableStatus = VariableStatus.PUBLISHED_INTERNAL))
        ).isTrue()
        assertThat(variableDefinitionService.hasRequiredFieldsForPublishing(
            SAVED_TO_PUBLISH_EMPTY_UNIT_TYPES, UpdateDraft(variableStatus = VariableStatus.PUBLISHED_INTERNAL))
        ).isFalse()
    }

    @Test
    fun `check duplicate shortname update`(){
        assertThat(variableDefinitionService.isUpdatedShortNameDuplicate(
            SAVED_INTERNAL_VARIABLE_DEFINITION, UpdateDraft(shortName = SAVED_INTERNAL_VARIABLE_DEFINITION.shortName))
        ).isFalse()
        assertThat(variableDefinitionService.isUpdatedShortNameDuplicate(
            SAVED_INTERNAL_VARIABLE_DEFINITION,
            UpdateDraft(shortName = "bus"))).isTrue()
    }

    @Test
    fun `check illegal shortname for publish`(){
        assertThat(variableDefinitionService.isIllegalShortNameForPublishing(
            SAVED_BYDEL_WITH_ILLEGAL_SHORTNAME,
            UpdateDraft(
                variableStatus = VariableStatus.PUBLISHED_INTERNAL
            ))).isTrue()
        assertThat(variableDefinitionService.isIllegalShortNameForPublishing(
            SAVED_TO_PUBLISH,
            UpdateDraft(
                shortName = ILLEGAL_SHORTNAME_KEYWORD,
                variableStatus = VariableStatus.PUBLISHED_INTERNAL
            )
        )).isTrue()
        assertThat(variableDefinitionService.isIllegalShortNameForPublishing(
            SAVED_BYDEL_WITH_ILLEGAL_SHORTNAME,
            UpdateDraft(
                shortName = "bydel",
                variableStatus = VariableStatus.PUBLISHED_INTERNAL
            ))).isFalse()
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
