package no.ssb.metadata.vardef.integrations.vardok.repositories

import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.vardok.models.VardokVardefIdPair
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.DRAFT_BUS_EXAMPLE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VardokIdMappingRepositoryTest: BaseVardefTest() {

    @Inject lateinit var vardokIdMappingRepository: VardokIdMappingRepository

    private val vardokVardefIdPair = (VardokVardefIdPair("2", DRAFT_BUS_EXAMPLE.definitionId))

    @BeforeEach
    fun beforeEach() {
        vardokIdMappingRepository.deleteAll()
        vardokIdMappingRepository.save(vardokVardefIdPair)
    }

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