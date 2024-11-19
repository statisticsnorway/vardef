package no.ssb.metadata.vardef.services

import no.ssb.metadata.vardef.models.*
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
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

    @Test
    fun `update owner team`() {
        val existingOwnerTeam = SAVED_DRAFT_DEADWEIGHT_EXAMPLE.owner.team
        val input =
            SAVED_DRAFT_DEADWEIGHT_EXAMPLE.copy().apply {
                this.owner =
                    Owner(
                        "my-last-team",
                        listOf("skip-stat-developers", TEST_DEVELOPERS_GROUP),
                    )
            }
        val updatedDraft = variableDefinitionService.update(input)
        assertThat(updatedDraft.owner.team).isNotEqualTo(existingOwnerTeam)
    }

    companion object {
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
                    "Published External on Deprecated",
                    SAVED_DEPRECATED_VARIABLE_DEFINITION.definitionId,
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
                    "Deprecated matching",
                    SAVED_DEPRECATED_VARIABLE_DEFINITION.definitionId,
                    VariableStatus.DEPRECATED,
                    SAVED_DEPRECATED_VARIABLE_DEFINITION.toCompleteResponse(),
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
