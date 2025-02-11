package no.ssb.metadata.vardef.models

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VariableStatusTest {
    @Test
    fun `variables can not transition to illegal states`() {
        val testStatusDraft = VariableStatus.DRAFT
        assertTrue(testStatusDraft.canTransitionTo(VariableStatus.PUBLISHED_INTERNAL))
        assertTrue(testStatusDraft.canTransitionTo(VariableStatus.PUBLISHED_EXTERNAL))

        val testStatusPublishedInternal = VariableStatus.PUBLISHED_INTERNAL
        assertTrue(testStatusPublishedInternal.canTransitionTo(VariableStatus.PUBLISHED_EXTERNAL))
        assertFalse(testStatusPublishedInternal.canTransitionTo(VariableStatus.DRAFT))

        val testStatusPublishedExternal = VariableStatus.PUBLISHED_EXTERNAL
        assertFalse(testStatusPublishedExternal.canTransitionTo(VariableStatus.PUBLISHED_INTERNAL))
        assertFalse(testStatusPublishedExternal.canTransitionTo(VariableStatus.DRAFT))
    }
}
