package no.ssb.metadata.model

import com.fasterxml.jackson.annotation.JsonIgnore
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.Id
import org.bson.types.ObjectId
import jakarta.validation.Valid
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoCollection
import io.micronaut.core.annotation.NonNull
import jakarta.inject.Singleton
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono


@MappedEntity
data class VariableDefinition(
    @field:Id
    @GeneratedValue
    @JsonIgnore
    var id: ObjectId?,
    var name: String,
    var definition: String
)


interface VariableDefinitionRepository {

    fun list(): Publisher<VariableDefinition>

    fun save(@Valid variable: VariableDefinition): Mono<VariableDefinition>
}

@Singleton
open class MongoDbVariableDefinitionRepository(
    private val mongoClient: MongoClient) : VariableDefinitionRepository {

    override fun save(@Valid variable: VariableDefinition): Mono<VariableDefinition> =
        Mono.from(collection.insertOne(variable))
            .map { variable }

    @NonNull
    override fun list(): Publisher<VariableDefinition> = collection.find()

    private val collection: MongoCollection<VariableDefinition>
        get() = mongoClient.getDatabase("vardef")
            .getCollection("variable_definitions", VariableDefinition::class.java)
}