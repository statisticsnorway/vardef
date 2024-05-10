package no.ssb.metadata.repositories

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoCollection
import io.micronaut.core.annotation.NonNull
import jakarta.inject.Singleton
import jakarta.validation.Valid
import no.ssb.metadata.models.VariableDefinition
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface VariableDefinitionRepository {

    fun list(): Flux<VariableDefinition>

    fun save(@Valid variable: VariableDefinition): Mono<VariableDefinition>
}

@Singleton
open class MongoDbVariableDefinitionRepository(
    private val mongoClient: MongoClient
) : VariableDefinitionRepository {

    override fun save(@Valid variable: VariableDefinition): Mono<VariableDefinition> =
        Mono.from(collection.insertOne(variable))
            .map { variable }

    @NonNull
    override fun list(): Flux<VariableDefinition> = Flux.from(collection.find())

    private val collection: MongoCollection<VariableDefinition>
        get() = mongoClient.getDatabase("vardef")
            .getCollection("variable_definitions", VariableDefinition::class.java)
}