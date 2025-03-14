package no.ssb.metadata.vardef.integrations.vardok.repositories

import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.DRAFT_BUS_EXAMPLE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VardokIdMappingRepositoryTest: BaseVardefTest() {

    @Test
    fun `check vardef id exists`(){
        assertThat(vardokIdMappingRepository.existsByVardefId(DRAFT_BUS_EXAMPLE.definitionId)).isTrue()
    }

    @Test
    fun `delete mapping by vardef id`(){
        assertThat(vardokIdMappingRepository.existsByVardefId(DRAFT_BUS_EXAMPLE.definitionId)).isTrue()
        vardokIdMappingRepository.deleteByVardefId(DRAFT_BUS_EXAMPLE.definitionId)
        assertThat(vardokIdMappingRepository.existsByVardefId(DRAFT_BUS_EXAMPLE.definitionId)).isFalse()
    }
}