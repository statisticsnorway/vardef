package no.ssb.metadata.vardef.services

import no.ssb.metadata.vardef.exceptions.DefinitionTextUnchangedException
import no.ssb.metadata.vardef.exceptions.InvalidValidFromException
import no.ssb.metadata.vardef.models.LanguageStringType
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.models.ValidityPeriod
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.INCOME_TAX_VP1_P1
import no.ssb.metadata.vardef.utils.TEST_USER
import no.ssb.metadata.vardef.utils.VALIDITY_PERIOD_TAX_EXAMPLE
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.AssertionsForClassTypes
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.util.stream.Stream

class ValidityPeriodsServiceTest : BaseVardefTest() {
    private val savedVariableDefinitionId = INCOME_TAX_VP1_P1.definitionId

    @Test
    fun `end validity period`() {
        val newValidityPeriodValidFrom = LocalDate.of(2024, 9, 2)
        val latestPatch = patches.latest(savedVariableDefinitionId)
        val patchEndValidityPeriod =
            validityPeriods.endLastValidityPeriod(
                savedVariableDefinitionId,
                newValidityPeriodValidFrom,
                TEST_USER,
            )

        assertThat(patchEndValidityPeriod.validUntil).isAfter(patchEndValidityPeriod.validFrom)
        assertThat(patchEndValidityPeriod.patchId).isEqualTo(latestPatch.patchId + 1)
        assertThat(patchEndValidityPeriod.validUntil).isEqualTo(newValidityPeriodValidFrom.minusDays(1))
    }

    @ParameterizedTest
    @CsvSource(
        "1990, 7",
        "1760, null",
        "3000, 6",
        nullValues = ["null"],
    )
    fun `get valid period at date`(
        year: Int,
        patchId: Int?,
    ) {
        assertThat(
            validityPeriods
                .getForDate(
                    INCOME_TAX_VP1_P1.definitionId,
                    LocalDate.of(year, 1, 1),
                )?.patchId,
        ).isEqualTo(patchId)
    }

    @ParameterizedTest
    @MethodSource("validFromTestCases")
    fun `validate valid_from values in new validity period`(
        inputObject: ValidityPeriod,
        expectSuccess: Boolean,
    ) {
        if (!expectSuccess) {
            assertThrows<InvalidValidFromException> {
                validityPeriods.create(
                    INCOME_TAX_VP1_P1.definitionId,
                    inputObject,
                    TEST_USER,
                )
            }
        } else {
            AssertionsForClassTypes
                .assertThat(
                    validityPeriods.create(
                        INCOME_TAX_VP1_P1.definitionId,
                        inputObject,
                        TEST_USER,
                    ),
                ).isInstanceOf(SavedVariableDefinition::class.java)
        }
    }

    @ParameterizedTest
    @MethodSource("definitionTextTestCases")
    fun `validate updated definition texts in new validity period`(
        inputObject: ValidityPeriod,
        expectSuccess: Boolean,
    ) {
        if (!expectSuccess) {
            assertThrows<DefinitionTextUnchangedException> {
                validityPeriods.create(
                    INCOME_TAX_VP1_P1.definitionId,
                    inputObject,
                    TEST_USER,
                )
            }
        } else {
            AssertionsForClassTypes
                .assertThat(
                    validityPeriods.create(
                        INCOME_TAX_VP1_P1.definitionId,
                        inputObject,
                        TEST_USER,
                    ),
                ).isInstanceOf(SavedVariableDefinition::class.java)
        }
    }

    @ParameterizedTest
    @MethodSource("createValidityPeriodsTestCases")
    fun `save new validity period`(inputData: ValidityPeriod) {
        val patchesBefore = patches.list(savedVariableDefinitionId)
        val newValidityPeriod =
            validityPeriods.create(
                savedVariableDefinitionId,
                inputData,
                TEST_USER,
            )
        val patchesAfter =
            patches.list(
                savedVariableDefinitionId,
            )

        val lastPatchInSecondToLastValidityPeriod =
            validityPeriods
                .getAsMap(savedVariableDefinitionId)
                .let { it.values.elementAt(it.values.size - 2) }
                ?.last()

        assertThat(patchesAfter.size).isEqualTo(patchesBefore.size + 2)
        assertThat(newValidityPeriod.patchId).isEqualTo(patchesBefore.last().patchId + 2)
        assertThat(newValidityPeriod.patchId).isEqualTo(patchesAfter.last().patchId)
        assertThat(newValidityPeriod.validFrom).isEqualTo(inputData.validFrom)
        assertThat(lastPatchInSecondToLastValidityPeriod?.validUntil).isEqualTo(
            newValidityPeriod.validFrom.minusDays(1),
        )
    }

    @Test
    fun `save new validity period before all valid from`() {
        val allPatches = patches.list(savedVariableDefinitionId)
        val saveNewValidityPeriod =
            validityPeriods.create(
                INCOME_TAX_VP1_P1.definitionId,
                VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                    validFrom = LocalDate.of(1796, 1, 1),
                    definition =
                        LanguageStringType(
                            nb = "Ny def",
                            nn = "Ny def",
                            en = "New def",
                        ),
                ),
                TEST_USER,
            )
        val patchesAfterSave = patches.list(savedVariableDefinitionId)

        assertThat(saveNewValidityPeriod.patchId).isEqualTo(allPatches.last().patchId + 1)
        assertThat(saveNewValidityPeriod.validUntil).isEqualTo(
            patchesAfterSave.first().validFrom.minusDays(1),
        )
        assertThat(saveNewValidityPeriod.validFrom).isBefore(patchesAfterSave.first().validFrom)
    }

    companion object {
        @JvmStatic
        fun createValidityPeriodsTestCases(): Stream<Arguments> =
            Stream.of(
                Arguments.argumentSet(
                    "Today's date",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        validFrom = LocalDate.now(),
                        definition =
                            LanguageStringType(
                                nb = "Ny def",
                                nn = "Ny def",
                                en = "New def",
                            ),
                    ),
                ),
                Arguments.argumentSet(
                    "2025-10-05",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        validFrom = LocalDate.of(2025, 10, 5),
                        definition =
                            LanguageStringType(
                                nb = "Ny def",
                                nn = "Ny def",
                                en = "New def",
                            ),
                    ),
                ),
                Arguments.argumentSet(
                    "2025-01-01",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        validFrom = LocalDate.of(2050, 1, 1),
                        definition =
                            LanguageStringType(
                                nb = "Ny def",
                                nn = "Ny def",
                                en = "New def",
                            ),
                    ),
                ),
            )

        @JvmStatic
        fun validFromTestCases(): Stream<Arguments> =
            Stream.of(
                Arguments.argumentSet(
                    "Between existing periods",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        definition =
                            LanguageStringType(
                                nb = "Endring",
                                nn = "Endring",
                                en = "Endring",
                            ),
                        validFrom = LocalDate.of(1990, 1, 1),
                    ),
                    false,
                ),
                Arguments.argumentSet(
                    "Before existing periods",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        definition =
                            LanguageStringType(
                                nb = "Endring",
                                nn = "Endring",
                                en = "Endring",
                            ),
                        validFrom = LocalDate.of(1760, 1, 1),
                    ),
                    true,
                ),
                Arguments.argumentSet(
                    "After existing periods",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        definition =
                            LanguageStringType(
                                nb = "Endring",
                                nn = "Endring",
                                en = "Endring",
                            ),
                        validFrom = LocalDate.of(3000, 1, 1),
                    ),
                    true,
                ),
            )

        @JvmStatic
        fun definitionTextTestCases(): Stream<Arguments> =
            Stream.of(
                Arguments.argumentSet(
                    "No change",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        definition =
                            LanguageStringType(
                                "Intektsskatt ny definisjon",
                                "Intektsskatt ny definisjon",
                                "Income tax new definition",
                            ),
                        validFrom = LocalDate.of(3000, 1, 1),
                    ),
                    false,
                ),
                Arguments.argumentSet(
                    "All languages appended",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        definition =
                            LanguageStringType(
                                nb = "Intektsskatt ny definisjon. Liten endring",
                                nn = "Intektsskatt ny definisjon. Liten endring",
                                en = "Income tax new definition. small change",
                            ),
                        validFrom = LocalDate.of(3000, 1, 1),
                    ),
                    true,
                ),
                Arguments.argumentSet(
                    "One language appended",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        definition =
                            LanguageStringType(
                                nb = "Intektsskatt ny definisjon. Liten endring",
                                nn = "Intektsskatt ny definisjon",
                                en = "Income tax new definition",
                            ),
                        validFrom = LocalDate.of(3000, 1, 1),
                    ),
                    false,
                ),
                Arguments.argumentSet(
                    "All languages completely new text",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        definition =
                            LanguageStringType(
                                nb = "Endring",
                                nn = "Endring",
                                en = "Endring",
                            ),
                        validFrom = LocalDate.of(3000, 1, 1),
                    ),
                    true,
                ),
                Arguments.argumentSet(
                    "All languages null",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        definition =
                            LanguageStringType(
                                nb = null,
                                nn = null,
                                en = null,
                            ),
                        validFrom = LocalDate.of(3000, 1, 1),
                    ),
                    false,
                ),
                Arguments.argumentSet(
                    "One language null",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        definition =
                            LanguageStringType(
                                nb = "Intektsskatt ny definisjon",
                                nn = null,
                                en = "Income tex new definition",
                            ),
                        validFrom = LocalDate.of(3000, 1, 1),
                    ),
                    false,
                ),
            )
    }
}
