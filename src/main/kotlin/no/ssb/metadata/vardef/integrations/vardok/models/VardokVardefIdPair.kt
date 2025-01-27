package no.ssb.metadata.vardef.integrations.vardok.models

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.model.naming.NamingStrategies
import io.micronaut.serde.annotation.Serdeable
import org.bson.types.ObjectId

@Serdeable
@MappedEntity(namingStrategy = NamingStrategies.Raw::class)
data class VardokVardefIdPair(
    var vardokId: String,
    var vardefId: String,
    @field:Id @GeneratedValue
    var id: ObjectId? = null,
)
