package no.ssb.metadata.vardef.repositories

import io.micronaut.data.mongodb.annotation.MongoRepository
import io.micronaut.data.repository.CrudRepository
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.models.VariableStatus
import org.bson.types.ObjectId

@MongoRepository(databaseName = "vardef")
interface VariableDefinitionRepository : CrudRepository<SavedVariableDefinition, ObjectId> {
    fun findByDefinitionIdOrderByPatchId(definitionId: String): List<SavedVariableDefinition>

    fun findByDefinitionIdAndPatchId(
        definitionId: String,
        patchId: Int,
    ): SavedVariableDefinition

    fun existsByShortName(shortName: String): Boolean

    fun findDistinctDefinitionIdByVariableStatusInList(variableStatus: List<VariableStatus>): Set<String>

    fun findByShortName(shortName: String): SavedVariableDefinition?
}
