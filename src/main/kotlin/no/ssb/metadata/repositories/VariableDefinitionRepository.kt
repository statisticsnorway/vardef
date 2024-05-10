package no.ssb.metadata.repositories

import io.micronaut.data.mongodb.annotation.MongoRepository
import io.micronaut.data.repository.CrudRepository
import no.ssb.metadata.models.VariableDefinition
import org.bson.types.ObjectId

@MongoRepository(databaseName = "vardef")
interface VariableDefinitionRepository : CrudRepository<VariableDefinition, ObjectId>
