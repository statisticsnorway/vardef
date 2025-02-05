package no.ssb.metadata.vardef.models

import io.micronaut.core.annotation.Nullable
import io.micronaut.data.annotation.*
import io.micronaut.data.model.naming.NamingStrategies
import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import no.ssb.metadata.vardef.constants.MEASUREMENT_TYPE_KLASS_CODE
import no.ssb.metadata.vardef.constants.SUBJECT_FIELDS_KLASS_CODE
import no.ssb.metadata.vardef.constants.UNIT_TYPES_KLASS_CODE
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
    var classificationReference: String?,
    var unitTypes: List<String>,
    var subjectFields: List<String>,
    var containsSpecialCategoriesOfPersonalData: Boolean,
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
    @NotNull
    var owner: Owner,
    var contact: Contact?,
    @DateCreated
    var createdAt: LocalDateTime,
    @Email
    var createdBy: String,
    @DateUpdated
    var lastUpdatedAt: LocalDateTime,
    @Email
    var lastUpdatedBy: String,
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
            classificationUri = classificationReference?.let { klassService.getKlassUrlForIdAndLanguage(it, language) },
            unitTypes = unitTypes.map { klassService.renderCode(UNIT_TYPES_KLASS_CODE, it, language) },
            subjectFields = subjectFields.map { klassService.renderCode(SUBJECT_FIELDS_KLASS_CODE, it, language) },
            containsSpecialCategoriesOfPersonalData = containsSpecialCategoriesOfPersonalData,
            measurementType = measurementType?.let { klassService.renderCode(MEASUREMENT_TYPE_KLASS_CODE, it, language) },
            validFrom = validFrom,
            validUntil = validUntil,
            externalReferenceUri = externalReferenceUri,
            comment = comment?.getValidLanguage(language),
            relatedVariableDefinitionUris = relatedVariableDefinitionUris?.map { URI(it).toURL() },
            contact = contact?.let { RenderedContact(contact?.title?.getValidLanguage(language), it.email) },
            lastUpdatedAt = lastUpdatedAt,
        )

    fun toPatch(): Patch =
        Patch(
            name = name,
            definition = definition,
            classificationReference = classificationReference,
            unitTypes = unitTypes,
            subjectFields = subjectFields,
            containsSpecialCategoriesOfPersonalData = containsSpecialCategoriesOfPersonalData,
            variableStatus = variableStatus,
            measurementType = measurementType,
            validUntil = validUntil,
            externalReferenceUri = externalReferenceUri,
            comment = comment,
            relatedVariableDefinitionUris = relatedVariableDefinitionUris?.map { URI(it).toURL() },
            owner = owner,
            contact = contact,
        )

    fun toCompleteResponse(): CompleteResponse =
        CompleteResponse(
            id = definitionId,
            patchId = patchId,
            name = name,
            shortName = shortName,
            definition = definition,
            classificationReference = classificationReference,
            unitTypes = unitTypes,
            subjectFields = subjectFields,
            containsSpecialCategoriesOfPersonalData = containsSpecialCategoriesOfPersonalData,
            variableStatus = variableStatus,
            measurementType = measurementType,
            validFrom = validFrom,
            validUntil = validUntil,
            externalReferenceUri = externalReferenceUri,
            comment = comment,
            relatedVariableDefinitionUris = relatedVariableDefinitionUris?.map { URI(it).toURL() },
            owner = owner,
            contact = contact,
            lastUpdatedAt = lastUpdatedAt,
            lastUpdatedBy = lastUpdatedBy,
            createdAt = createdAt,
            createdBy = createdBy,
        )

    fun copyAndUpdate(
        varDefUpdates: UpdateDraft,
        userName: String,
    ): SavedVariableDefinition =
        copy(
            name = varDefUpdates.name?.let { name.update(it) } ?: name,
            shortName = varDefUpdates.shortName ?: shortName,
            definition = varDefUpdates.definition?.let { definition.update(it) } ?: definition,
            classificationReference = varDefUpdates.classificationReference ?: classificationReference,
            unitTypes = varDefUpdates.unitTypes ?: unitTypes,
            subjectFields = varDefUpdates.subjectFields ?: subjectFields,
            containsSpecialCategoriesOfPersonalData =
                varDefUpdates.containsSpecialCategoriesOfPersonalData ?: containsSpecialCategoriesOfPersonalData,
            variableStatus = varDefUpdates.variableStatus ?: variableStatus,
            measurementType = varDefUpdates.measurementType ?: measurementType,
            validFrom = varDefUpdates.validFrom ?: validFrom,
            validUntil = varDefUpdates.validUntil ?: validUntil,
            externalReferenceUri = varDefUpdates.externalReferenceUri ?: externalReferenceUri,
            comment = varDefUpdates.comment?.let { comment?.update(it) } ?: comment,
            relatedVariableDefinitionUris =
                varDefUpdates.relatedVariableDefinitionUris?.map { it.toString() } ?: relatedVariableDefinitionUris,
            owner = varDefUpdates.owner ?: owner,
            contact = varDefUpdates.contact ?: contact,
            lastUpdatedBy = userName,
        )
}
