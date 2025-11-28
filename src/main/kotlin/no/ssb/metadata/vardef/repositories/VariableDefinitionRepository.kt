package no.ssb.metadata.vardef.repositories

import io.micronaut.data.mongodb.annotation.MongoRepository
import io.micronaut.data.repository.CrudRepository
import io.micronaut.data.repository.reactive.ReactiveStreamsPageableRepository
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.models.VariableStatus
import org.bson.types.ObjectId
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono

const val MONGO_DB_NAME_VARDEF = "vardef"

@MongoRepository(databaseName = MONGO_DB_NAME_VARDEF)
interface VariableDefinitionRepository : ReactiveStreamsPageableRepository<SavedVariableDefinition, ObjectId> {
    fun findByDefinitionIdOrderByPatchId(definitionId: String): Publisher<SavedVariableDefinition>

    fun findByDefinitionIdAndPatchId(
        definitionId: String,
        patchId: Int,
    ): Mono<SavedVariableDefinition>

    fun existsByShortName(shortName: String): Mono<Boolean>

    fun findDistinctDefinitionIdByVariableStatusInList(variableStatus: List<VariableStatus>): Publisher<String>

    fun findByShortName(shortName: String): Mono<SavedVariableDefinition>

    fun findDistinctDefinitionIdByShortName(shortName: String): Mono<String>
}
