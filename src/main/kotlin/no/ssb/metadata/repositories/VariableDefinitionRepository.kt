package no.ssb.metadata.repositories

import io.micronaut.data.mongodb.annotation.MongoRepository
import io.micronaut.data.repository.CrudRepository
import no.ssb.metadata.models.VariableDefinitionDAO
import org.bson.types.ObjectId

@MongoRepository(databaseName = "vardef")
interface VariableDefinitionRepository : CrudRepository<VariableDefinitionDAO, ObjectId> {
    fun findByDefinitionId(definitionId: String): VariableDefinitionDAO
}
