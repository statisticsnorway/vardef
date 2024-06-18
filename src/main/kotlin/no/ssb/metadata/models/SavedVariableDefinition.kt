package no.ssb.metadata.models

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.model.naming.NamingStrategies
import jakarta.validation.constraints.Pattern
import java.util.Optional
import org.bson.types.ObjectId

@MappedEntity(namingStrategy = NamingStrategies.Raw::class)
data class SavedVariableDefinition(
    var definitionId: String,
    @field:Id @GeneratedValue
    var id: ObjectId? = null,
    var name: LanguageStringType,
    @Pattern(regexp = "^[a-z0-9_]{3,}$")
    var shortName: String,
    var definition: LanguageStringType,
    var classificationUri: String,
    var unitTypes: List<KlassReference>,
    var subjectFields: List<KlassReference>,
    var containsUnitIdentifyingInformation: Boolean,
    var containsSensitivePersonalInformation: Boolean,
    var variableStatus: VariableStatus,
    var measurementType: KlassReference,
    var validFrom: String,
    var validUntil: String?,
    var externalReferenceUri: String?,
    var relatedVariableDefinitionUris: List<String>?,
    var owner: Owner?,
    var contact: Contact,
    var createdAt: String,
    var createdBy: Person?,
    var lastUpdatedAt: String,
    var lastUpdatedBy: Person?,
) {
    fun toRenderedVariableDefinition(language: SupportedLanguages): RenderedVariableDefinition =
        RenderedVariableDefinition(
            id = definitionId,
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
            lastUpdatedBy = lastUpdatedBy,
        )

    fun toInputVariableDefinition(): InputVariableDefinition =
        InputVariableDefinition(
            id = definitionId,
            name = name,
            shortName = shortName,
            definition = definition,
            // TODO
            classificationReference = "",
            // TODO
            unitTypes = emptyList(),
            // TODO
            subjectFields = emptyList(),
            containsUnitIdentifyingInformation = containsUnitIdentifyingInformation.toString(),
            containsSensitivePersonalInformation = containsSensitivePersonalInformation.toString(),
            variableStatus = variableStatus,
            // TODO
            measurementType = "",
            validFrom = validFrom,
            validUntil = validUntil,
            externalReferenceUri = externalReferenceUri,
            relatedVariableDefinitionUris = relatedVariableDefinitionUris,
            contact = contact,
        )
}
