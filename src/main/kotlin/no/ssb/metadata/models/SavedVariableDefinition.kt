package no.ssb.metadata.models

import io.micronaut.core.annotation.Nullable
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.model.naming.NamingStrategies
import org.bson.types.ObjectId

@MappedEntity(namingStrategy = NamingStrategies.Raw::class)
data class SavedVariableDefinition(
    var definitionId: String,
    @field:Id @GeneratedValue
    var id: ObjectId? = null,
    var name: LanguageStringType,
    var shortName: String,
    var definition: LanguageStringType,
    @Nullable
    var classificationUri: String?,
    var unitTypes: List<KlassReference>,
    var subjectFields: List<KlassReference>,
    var containsUnitIdentifyingInformation: Boolean,
    var containsSensitivePersonalInformation: Boolean,
    var variableStatus: VariableStatus,
    @Nullable
    var measurementType: KlassReference?,
    var validFrom: String,
    @Nullable
    var validUntil: String?,
    @Nullable
    var externalReferenceUri: String?,
    @Nullable
    var relatedVariableDefinitionUris: List<String>?,
    @Nullable
    var owner: Owner?,
    var contact: Contact,
    var createdAt: String,
    @Nullable
    var createdBy: Person?,
    var lastUpdatedAt: String,
    @Nullable
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
            variableStatus = variableStatus.toString(),
            // TODO
            measurementType = "",
            validFrom = validFrom,
            validUntil = validUntil,
            externalReferenceUri = externalReferenceUri,
            relatedVariableDefinitionUris = relatedVariableDefinitionUris,
            contact = contact,
        )

    fun copyAndUpdate(varDefUpdates: UpdateVariableDefinition): SavedVariableDefinition =
        copy(
            // Carry over value from existing object
            id = id,
            definitionId = definitionId,
            owner = owner,
            createdAt = createdAt,
            createdBy = createdBy,
            // TODO DPMETA-268
            lastUpdatedAt = createdAt,
            // TODO DPMETA-268
            lastUpdatedBy = createdBy,
            // Update field if non-null value provided
            name = varDefUpdates.name ?: name,
            shortName = varDefUpdates.shortName ?: shortName,
            definition = varDefUpdates.definition ?: definition,
            // TODO DPMETA-257 convert reference to URI
            classificationUri = varDefUpdates.classificationReference ?: classificationUri,
            // TODO DPMETA-257
            unitTypes = listOf(KlassReference("https://example.com/", "", "")),
            // TODO DPMETA-257
            subjectFields = listOf(KlassReference("https://example.com/", "", "")),
            containsUnitIdentifyingInformation =
                varDefUpdates.containsUnitIdentifyingInformation
                    ?: containsUnitIdentifyingInformation,
            containsSensitivePersonalInformation =
                varDefUpdates.containsSensitivePersonalInformation
                    ?: containsSensitivePersonalInformation,
            variableStatus = variableStatus,
            // TODO DPMETA-257
            measurementType = KlassReference("https://example.com/", "", ""),
            validFrom = varDefUpdates.validFrom ?: validFrom,
            validUntil = varDefUpdates.validUntil ?: validUntil,
            externalReferenceUri = varDefUpdates.externalReferenceUri ?: externalReferenceUri,
            relatedVariableDefinitionUris = varDefUpdates.relatedVariableDefinitionUris ?: relatedVariableDefinitionUris,
            contact = varDefUpdates.contact ?: contact,
        )
}
