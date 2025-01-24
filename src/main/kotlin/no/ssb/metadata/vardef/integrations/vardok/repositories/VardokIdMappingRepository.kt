package no.ssb.metadata.vardef.integrations.vardok.repositories

import io.micronaut.data.mongodb.annotation.MongoRepository
import io.micronaut.data.repository.CrudRepository
import no.ssb.metadata.vardef.integrations.vardok.models.VardokVardefIdPair
import org.bson.types.ObjectId

@MongoRepository(databaseName = "vardok-id-mapping")
interface VardokIdMappingRepository : CrudRepository<VardokVardefIdPair, ObjectId> {
    fun getVardefIdByVardokId(vardokId: String): String?
}
