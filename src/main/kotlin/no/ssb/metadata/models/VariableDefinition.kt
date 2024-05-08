package no.ssb.metadata.models

import com.fasterxml.jackson.annotation.JsonIgnore
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.Id
import org.bson.types.ObjectId
import jakarta.validation.Valid
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoCollection
import io.micronaut.core.annotation.NonNull
import io.micronaut.core.convert.ConversionContext
import io.micronaut.core.convert.TypeConverter
import jakarta.inject.Singleton
import no.ssb.metadata.exceptions.UnknownLanguageException
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono
import java.util.*


enum class SupportedLanguages (val code: String) {
    NORWEGIAN_BOKMÅL("nb"),
    NORWEGIAN_NYNORSK("nn"),
    ENGLISH("en");

    override fun toString(): String {
        return code
    }
}

@Singleton
class SupportedLanguagesConverter : TypeConverter<String, SupportedLanguages> {
    override fun convert(code: String, targetType: Class<SupportedLanguages>, context: ConversionContext): Optional<SupportedLanguages> {
        return Optional.of(SupportedLanguages.entries.firstOrNull() { it.code == code } ?: throw UnknownLanguageException("Unknown language code $code. Valid values are ${SupportedLanguages.entries.map { it.code }}"))
    }
}


@MappedEntity
data class VariableDefinition(
    @field:Id
    @GeneratedValue
    @JsonIgnore
    var id: ObjectId?,
    var name: Map<SupportedLanguages, String>,
    var definition: Map<SupportedLanguages, String>
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