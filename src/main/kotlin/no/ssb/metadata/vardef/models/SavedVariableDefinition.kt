package no.ssb.metadata.vardef.models

import io.micronaut.core.annotation.Nullable
import io.micronaut.data.annotation.*
import io.micronaut.data.model.naming.NamingStrategies
import io.micronaut.serde.annotation.Serdeable
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import org.bson.types.ObjectId
import java.net.URI
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Saved variable definition
 *
 * The object which is persisted to the data store. This should not be exposed externally.
 */
@Serdeable
@MappedEntity(namingStrategy = NamingStrategies.Raw::class)
data class SavedVariableDefinition(
    var definitionId: String,
    @field:Id @GeneratedValue
    var id: ObjectId? = null,
    var patchId: Int,
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
    var comment: LanguageStringType?,
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
    /**
     * Render the variable definition, so it's suitable for display to humans.
     *
     * @param language The language to render in.
     * @param klassService The service from which to obtain details for classification codes.
     * @return The rendered object.
     */
    fun render(
        language: SupportedLanguages,
        klassService: KlassService,
    ): RenderedVariableDefinition =
        RenderedVariableDefinition(
            id = definitionId,
            patchId = patchId,
            name = name.getValidLanguage(language),
            shortName = shortName,
            definition = definition.getValidLanguage(language),
            classificationUri = classificationUri?.let { klassService.getKlassUrlForIdAndLanguage(it, language) },
            unitTypes = unitTypes.map { klassService.getCodeItemFor("702", it, language) },
            subjectFields = subjectFields.map { klassService.getCodeItemFor("618", it, language) },
            containsSensitivePersonalInformation = containsSensitivePersonalInformation,
            measurementType = measurementType?.let { klassService.getCodeItemFor("303", it, language) },
            validFrom = validFrom,
            validUntil = validUntil,
            externalReferenceUri = externalReferenceUri,
            comment = comment?.getValidLanguage(language),
            relatedVariableDefinitionUris = relatedVariableDefinitionUris?.map { URI(it).toURL() },
            contact = contact?.let { RenderedContact(contact?.title?.getValidLanguage(language), it.email) },
            lastUpdatedAt = lastUpdatedAt,
        )

    fun toDraft(): Draft =
        Draft(
            id = definitionId,
            name = name,
            shortName = shortName,
            definition = definition,
            classificationReference = classificationUri?.split("/")?.lastOrNull(),
            unitTypes = unitTypes,
            subjectFields = subjectFields,
            containsSensitivePersonalInformation = containsSensitivePersonalInformation,
            variableStatus = variableStatus,
            measurementType = measurementType,
            validFrom = validFrom,
            validUntil = validUntil,
            externalReferenceUri = externalReferenceUri,
            comment = comment,
            relatedVariableDefinitionUris = relatedVariableDefinitionUris?.map { URI(it).toURL() },
            contact = contact,
        )

    fun toPatch(): Patch =
        Patch(
            name = name,
            definition = definition,
            classificationReference = classificationUri?.split("/")?.lastOrNull(),
            unitTypes = unitTypes,
            subjectFields = subjectFields,
            containsSensitivePersonalInformation = containsSensitivePersonalInformation,
            variableStatus = variableStatus,
            measurementType = measurementType,
            validUntil = validUntil,
            externalReferenceUri = externalReferenceUri,
            comment = comment,
            relatedVariableDefinitionUris = relatedVariableDefinitionUris?.map { URI(it).toURL() },
            contact = contact,
        )

    fun toCompleteResponse(): CompleteResponse =
        CompleteResponse(
            id = definitionId,
            patchId = patchId,
            name = name,
            shortName = shortName,
            definition = definition,
            classificationReference = classificationUri?.split("/")?.lastOrNull(),
            unitTypes = unitTypes,
            subjectFields = subjectFields,
            containsSensitivePersonalInformation = containsSensitivePersonalInformation,
            variableStatus = variableStatus,
            measurementType = measurementType,
            validFrom = validFrom,
            validUntil = validUntil,
            externalReferenceUri = externalReferenceUri,
            comment = comment,
            relatedVariableDefinitionUris = relatedVariableDefinitionUris?.map { URI(it).toURL() },
            contact = contact,
            lastUpdatedAt = lastUpdatedAt,
            lastUpdatedBy = lastUpdatedBy,
            createdAt = createdAt,
            createdBy = createdBy,
        )

    fun copyAndUpdate(varDefUpdates: UpdateDraft): SavedVariableDefinition =
        copy(
            name = varDefUpdates.name ?: name,
            shortName = varDefUpdates.shortName ?: shortName,
            definition = varDefUpdates.definition ?: definition,
            // TODO DPMETA-257 convert reference to URI
            classificationUri = varDefUpdates.classificationReference ?: classificationUri,
            unitTypes = varDefUpdates.unitTypes ?: unitTypes,
            subjectFields = varDefUpdates.subjectFields ?: subjectFields,
            containsSensitivePersonalInformation =
                varDefUpdates.containsSensitivePersonalInformation ?: containsSensitivePersonalInformation,
            variableStatus = varDefUpdates.variableStatus ?: variableStatus,
            measurementType = varDefUpdates.measurementType ?: measurementType,
            validFrom = varDefUpdates.validFrom ?: validFrom,
            validUntil = varDefUpdates.validUntil ?: validUntil,
            externalReferenceUri = varDefUpdates.externalReferenceUri ?: externalReferenceUri,
            relatedVariableDefinitionUris =
                varDefUpdates.relatedVariableDefinitionUris?.map { it.toString() } ?: relatedVariableDefinitionUris,
            contact = varDefUpdates.contact ?: contact,
        )
}
