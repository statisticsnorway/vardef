package no.ssb.metadata.models

import com.fasterxml.jackson.annotation.JsonIgnore
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.model.naming.NamingStrategies
import io.swagger.v3.oas.annotations.media.Schema
import io.viascom.nanoid.NanoId
import org.bson.types.ObjectId

@MappedEntity(namingStrategy = NamingStrategies.Raw::class)
data class SavedVariableDefinition(
    @field:Id @GeneratedValue @JsonIgnore val mongoId: ObjectId?,
    var name: LanguageStringType,
    var shortName: String,
    var definition: LanguageStringType,
    var classificationUri: String,
    var unitTypes: List<KlassReference>,
    var subjectFields: List<KlassReference>,
    var containsUnitIdentifyingInformation: Boolean,
    var containsSensitivePersonalInformation: Boolean,
    var variableStatus: String,
    var measurementType: KlassReference,
    var validFrom: String,
    var validUntil: String,
    var externalReferenceUri: String,
    var relatedVariableDefinitionUris: List<String>,
    var owner: Owner?,
    var contact: Contact,
    var createdAt: String,
    var createdBy: Person?,
    var lastUpdatedAt: String,
    var lastUpdatedBy: Person?,
    @JsonIgnore val id: String? = NanoId.generate(8),

    ) {
    fun toRenderedVariableDefinition(language: SupportedLanguages): RenderedVariableDefinition =
        RenderedVariableDefinition(
            id = id,
            name = name.getValidLanguage(language),
            shortName = shortName,
            definition = definition.getValidLanguage(language),
            classificationUri = classificationUri,
            unitTypes = unitTypes,
            subjectFields = subjectFields,
            containsUnitIdentifyingInformation = containsUnitIdentifyingInformation,
            containsSensitivePersonalInformation = containsSensitivePersonalInformation,
            variableStatus = variableStatus,
            measurementType = measurementType,
            validFrom = validFrom,
            validUntil = validUntil,
            externalReferenceUri = externalReferenceUri,
            relatedVariableDefinitionUris = relatedVariableDefinitionUris,
            owner = owner,
            contact = RenderedContact(contact.title.getValidLanguage(language), contact.email),
            createdAt = createdAt,
            createdBy = createdBy,
            lastUpdatedAt = lastUpdatedAt,
            lastUpdatedBy = lastUpdatedBy
        )

    fun toInputVariableDefinition(): InputVariableDefinition =
        InputVariableDefinition(
            name = name,
            shortName = shortName,
            definition = definition,
            classificationReference = "",
            unitTypes = emptyList(),
            subjectFields = emptyList(),
            containsUnitIdentifyingInformation = containsUnitIdentifyingInformation,
            containsSensitivePersonalInformation = containsSensitivePersonalInformation,
            variableStatus = variableStatus,
            measurementType = "",
            validFrom = validFrom,
            validUntil = validUntil,
            externalReferenceUri = externalReferenceUri,
            relatedVariableDefinitionUris = relatedVariableDefinitionUris,
            contact = contact,

        )

}
