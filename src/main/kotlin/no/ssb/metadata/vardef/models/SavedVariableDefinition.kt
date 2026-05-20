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
 *
 * WARNING: Changes to field names or data types in this class are likely
 *      to break the app. In most cases a database migration will need to be performed.
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
    @NotNull
    var contact: Contact,
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
    ): RenderedView =
        RenderedView(
            id = definitionId,
            patchId = patchId,
            name = name.getValue(language),
            shortName = shortName,
            definition = definition.getValue(language),
            classificationUri = classificationReference?.let { klassService.getKlassUrlForIdAndLanguage(it, language) },
            unitTypes = unitTypes.map { klassService.renderCode(UNIT_TYPES_KLASS_CODE, it, language) },
            subjectFields = subjectFields.map { klassService.renderCode(SUBJECT_FIELDS_KLASS_CODE, it, language) },
            containsSpecialCategoriesOfPersonalData = containsSpecialCategoriesOfPersonalData,
            variableStatus = variableStatus,
            measurementType = measurementType?.let { klassService.renderCode(MEASUREMENT_TYPE_KLASS_CODE, it, language) },
            validFrom = validFrom,
            validUntil = validUntil,
            externalReferenceUri = externalReferenceUri,
            comment = comment?.getValue(language),
            relatedVariableDefinitionUris = relatedVariableDefinitionUris?.map { URI(it).toURL() },
            owner = owner,
            contact = contact.title.getValue(language).let { RenderedContact(it ?: "", contact.email) },
            lastUpdatedAt = lastUpdatedAt,
            lastUpdatedBy = lastUpdatedBy,
            createdAt = createdAt,
            createdBy = createdBy,
        )

    fun toPatch(): CreatePatch =
        CreatePatch(
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

    fun toCompleteView(): CompleteView =
        CompleteView(
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
            name = name.update(varDefUpdates.name),
            shortName = varDefUpdates.shortName ?: shortName,
            definition = definition.update(varDefUpdates.definition),
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
            comment = comment?.update(varDefUpdates.comment) ?: varDefUpdates.comment,
            relatedVariableDefinitionUris =
                varDefUpdates.relatedVariableDefinitionUris?.map { it.toString() } ?: relatedVariableDefinitionUris,
            owner = varDefUpdates.owner ?: owner,
            contact = varDefUpdates.contact ?: contact,
            lastUpdatedBy = userName,
        )

    fun copyAndUpdate(
        varDefUpdates: UpdateDraftPatch,
        userName: String,
    ): SavedVariableDefinition =
        copy(
            name =
                when (val updates = varDefUpdates.name) {
                    PatchField.Undefined -> name
                    is PatchField.Present -> name.update(updates.value)
                },
            shortName = varDefUpdates.shortName.orElse(shortName),
            definition =
                when (val updates = varDefUpdates.definition) {
                    PatchField.Undefined -> definition
                    is PatchField.Present -> definition.update(updates.value)
                },
            classificationReference = varDefUpdates.classificationReference.applyNullable(classificationReference),
            unitTypes = varDefUpdates.unitTypes.orElse(unitTypes),
            subjectFields = varDefUpdates.subjectFields.orElse(subjectFields),
            containsSpecialCategoriesOfPersonalData =
                varDefUpdates.containsSpecialCategoriesOfPersonalData.orElse(containsSpecialCategoriesOfPersonalData),
            variableStatus = varDefUpdates.variableStatus.orElse(variableStatus),
            measurementType = varDefUpdates.measurementType.applyNullable(measurementType),
            validFrom = varDefUpdates.validFrom.orElse(validFrom),
            validUntil = varDefUpdates.validUntil.applyNullable(validUntil),
            externalReferenceUri = varDefUpdates.externalReferenceUri.applyNullable(externalReferenceUri),
            comment =
                when (val updates = varDefUpdates.comment) {
                    PatchField.Undefined -> comment
                    is PatchField.Present -> updates.value?.let { comment?.update(it) ?: it }
                },
            relatedVariableDefinitionUris =
                when (val updates = varDefUpdates.relatedVariableDefinitionUris) {
                    PatchField.Undefined -> relatedVariableDefinitionUris
                    is PatchField.Present -> updates.value?.map { it.toString() }
                },
            owner = varDefUpdates.owner.orElse(owner),
            contact = varDefUpdates.contact.orElse(contact),
            lastUpdatedBy = userName,
        )
}
