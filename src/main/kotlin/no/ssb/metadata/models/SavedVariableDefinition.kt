package no.ssb.metadata.models

import io.micronaut.core.annotation.Nullable
import io.micronaut.data.annotation.*
import io.micronaut.data.model.naming.NamingStrategies
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import org.bson.types.ObjectId
import java.net.URI
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime

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
    var unitTypes: List<String>,
    var subjectFields: List<String>,
    var containsSensitivePersonalInformation: Boolean,
    var variableStatus: VariableStatus,
    @Nullable
    var measurementType: String?,
    var validFrom: LocalDate,
    @Nullable
    var validUntil: LocalDate?,
    @Nullable
    var externalReferenceUri: URL?,
    @Nullable
    var relatedVariableDefinitionUris: List<String>?,
    @Nullable
    var owner: Owner?,
    var contact: Contact?,
    @DateCreated
    var createdAt: LocalDateTime,
    @Nullable
    var createdBy: Person?,
    @DateUpdated
    var lastUpdatedAt: LocalDateTime,
    @Nullable
    var lastUpdatedBy: Person?,
) {
    fun toRenderedVariableDefinition(
        language: SupportedLanguages,
        klassService: KlassService,
    ): RenderedVariableDefinition =
        RenderedVariableDefinition(
            id = definitionId,
            name = name.getValidLanguage(language),
            shortName = shortName,
            definition = definition.getValidLanguage(language),
            classificationUri = classificationUri,
            unitTypes = unitTypes.map { klassService.getCodeItemFor("702", it, language) },
            subjectFields = subjectFields.map { klassService.getCodeItemFor("618", it, language) },
            containsSensitivePersonalInformation = containsSensitivePersonalInformation,
            variableStatus = variableStatus,
            measurementType = measurementType?.let { klassService.getCodeItemFor("303", it, language) },
            validFrom = validFrom,
            validUntil = validUntil,
            externalReferenceUri = externalReferenceUri,
            relatedVariableDefinitionUris = relatedVariableDefinitionUris?.map { URI(it).toURL() },
            owner = owner,
            contact = RenderedContact(contact?.title?.getValidLanguage(language), contact?.email),
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
            unitTypes = unitTypes,
            subjectFields = subjectFields,
            containsSensitivePersonalInformation = containsSensitivePersonalInformation,
            variableStatus = variableStatus,
            measurementType = measurementType,
            validFrom = validFrom,
            validUntil = validUntil,
            externalReferenceUri = externalReferenceUri,
            relatedVariableDefinitionUris = relatedVariableDefinitionUris?.map { URI(it).toURL() },
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
            unitTypes = varDefUpdates.unitTypes ?: unitTypes,
            subjectFields = varDefUpdates.subjectFields ?: subjectFields,
            containsSensitivePersonalInformation =
                varDefUpdates.containsSensitivePersonalInformation
                    ?: containsSensitivePersonalInformation,
            variableStatus = varDefUpdates.variableStatus ?: variableStatus,
            measurementType = varDefUpdates.measurementType ?: measurementType,
            validFrom = varDefUpdates.validFrom ?: validFrom,
            validUntil = varDefUpdates.validUntil ?: validUntil,
            externalReferenceUri = varDefUpdates.externalReferenceUri ?: externalReferenceUri,
            relatedVariableDefinitionUris =
                varDefUpdates.relatedVariableDefinitionUris?.map {
                    it.toString()
                } ?: relatedVariableDefinitionUris,
            contact = varDefUpdates.contact ?: contact,
        )
}
