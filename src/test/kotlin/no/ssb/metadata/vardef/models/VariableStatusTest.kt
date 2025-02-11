package no.ssb.metadata.vardef.models

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VariableStatusTest {
    @ParameterizedTest
    @MethodSource("provideTransitions")
    fun `variables can not transition to illegal states`(
        initialStatus: VariableStatus,
        targetStatus: VariableStatus,
        expected: Boolean,
    ) {
        assertEquals(expected, initialStatus.canTransitionTo(targetStatus))
    }

    companion object {
        @JvmStatic
        fun provideTransitions(): List<Arguments> =
            listOf(
                Arguments.of(VariableStatus.DRAFT, VariableStatus.DRAFT, true),
                Arguments.of(VariableStatus.DRAFT, VariableStatus.PUBLISHED_INTERNAL, true),
                Arguments.of(VariableStatus.DRAFT, VariableStatus.PUBLISHED_EXTERNAL, true),
                Arguments.of(VariableStatus.PUBLISHED_INTERNAL, VariableStatus.PUBLISHED_INTERNAL, true),
                Arguments.of(VariableStatus.PUBLISHED_INTERNAL, VariableStatus.PUBLISHED_EXTERNAL, true),
                Arguments.of(VariableStatus.PUBLISHED_INTERNAL, VariableStatus.DRAFT, false),
                Arguments.of(VariableStatus.PUBLISHED_EXTERNAL, VariableStatus.PUBLISHED_EXTERNAL, true),
                Arguments.of(VariableStatus.PUBLISHED_EXTERNAL, VariableStatus.PUBLISHED_INTERNAL, false),
                Arguments.of(VariableStatus.PUBLISHED_EXTERNAL, VariableStatus.DRAFT, false),
            )
    }
}
