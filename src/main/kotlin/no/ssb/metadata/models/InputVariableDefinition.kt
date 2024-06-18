package no.ssb.metadata.models

import io.micronaut.core.annotation.Nullable
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import io.viascom.nanoid.NanoId
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import no.ssb.metadata.constants.*
import no.ssb.metadata.validators.ValidBoolean
import no.ssb.metadata.validators.ValidDate
import no.ssb.metadata.validators.ValidUrl

@MappedEntity
@Serdeable(naming = SnakeCaseStrategy::class)
@Schema(
    example = """
        {
            "name":
                {   "en": "Country Background",
                    "nb": "Landbakgrunn",
                    "nn": "Landbakgrunn"
                },
            "short_name": "landbak",
            "definition":
                {
                    "en": "Country background is the person's own, the mother's or possibly the father's country of birth. Persons without an immigrant background always have Norway as country background. In cases where the parents have different countries of birth the mother's country of birth is chosen. If neither the person nor the parents are born abroad, country background is chosen from the first person born abroad in the order mother's mother, mother's father, father's mother, father's father.",
                    "nb": "For personer født i utlandet, er dette (med noen få unntak) eget fødeland. For personer født i Norge er det foreldrenes fødeland. I de tilfeller der foreldrene har ulikt fødeland, er det morens fødeland som blir valgt. Hvis ikke personen selv eller noen av foreldrene er utenlandsfødt, hentes landbakgrunn fra de første utenlandsfødte en treffer på i rekkefølgen mormor, morfar, farmor eller farfar.",
                    "nn": "For personar fødd i utlandet, er dette (med nokre få unntak) eige fødeland. For personar fødd i Noreg er det fødelandet til foreldra. I dei tilfella der foreldra har ulikt fødeland, er det fødelandet til mora som blir valt. Viss ikkje personen sjølv eller nokon av foreldra er utenlandsfødt, blir henta landsbakgrunn frå dei første utenlandsfødte ein treffar på i rekkjefølgja mormor, morfar, farmor eller farfar."
                }
            "classification_reference": "91",
            "unit_types": ["01", "02"],
            "subject_fields": ["he04"],
            "contains_unit_identifying_information": true,
            "contains_sensitive_personal_information": true,
            "variable_status": "Draft",
            "measurement_type": "volume", 
            "valid_from": "2024-06-05",
            "valid_until": "2024-06-05",
            "external_reference_uri": "https://example.com/",
            "relevant_variable_definition_uri": [
                "https://example.com/"
            ],
            "contact": {
                "title": "",
                "email": ""
            }
        }
    """,
)
data class InputVariableDefinition(
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @Nullable
    val id: String?,
    @Schema(description = NAME_FIELD_DESCRIPTION)
    val name: LanguageStringType,
    @Schema(description = SHORT_NAME_FIELD_DESCRIPTION)
    @Pattern(regexp = "^[a-z0-9_]{3,}$")
    val shortName: String,
    @Schema(description = DEFINITION_FIELD_DESCRIPTION)
    val definition: LanguageStringType,
    @Schema(description = CLASSIFICATION_REFERENCE_FIELD_DESCRIPTION)
    @Pattern(regexp = "^[0-9]+$")
    val classificationReference: String, // TODO Validate against klass data
    @Schema(description = UNIT_TYPES_FIELD_DESCRIPTION)
    @NotEmpty
    val unitTypes: List<String>, // TODO Validate against klass data
    @Schema(description = SUBJECT_FIELDS_FIELD_DESCRIPTION)
    @NotEmpty
    val subjectFields: List<String>, // TODO Validate against klass data
    @Schema(description = CONTAINS_UNIT_IDENTIFYING_INFORMATION_FIELD_DESCRIPTION)
    @NotNull
    @ValidBoolean(message = "Invalid value for contains_unit_identifying_information, must be either true or false")
    val containsUnitIdentifyingInformation: String,
    @Schema(description = CONTAINS_SENSITIVE_PERSONAL_INFORMATION_FIELD_DESCRIPTION)
    @NotNull
    @ValidBoolean(message = "Invalid value for contains_sensitive_personal_information, must be either true or false")
    val containsSensitivePersonalInformation: String,
    @Schema(description = VARIABLE_STATUS_FIELD_DESCRIPTION)
    val variableStatus: VariableStatus,
    @Schema(description = MEASURMENT_TYPE_FIELD_DESCRIPTION)
    @Nullable
    val measurementType: String?,
    @Schema(description = VALID_FROM_FIELD_DESCRIPTION)
    @ValidDate
    val validFrom: String,
    @Schema(description = VALID_UNTIL_FIELD_DESCRIPTION)
    @Nullable
    @ValidDate
    val validUntil: String?,
    @Schema(description = EXTERNAL_REFERENCE_URI_FIELD_DESCRIPTION)
    @Nullable
    @ValidUrl(message = "Website URL must be valid")
    val externalReferenceUri: String?,
    @Schema(description = RELATED_VARIABLE_DEFINITION_URIS_FIELD_DESCRIPTION)
    @Nullable
    val relatedVariableDefinitionUris: List<@ValidUrl String>?,
    @Schema(description = CONTACT_FIELD_DESCRIPTION)
    @Valid
    val contact: Contact,
) {
    fun toSavedVariableDefinition(): SavedVariableDefinition =
        SavedVariableDefinition(
            definitionId = NanoId.generate(8),
            name = name,
            shortName = shortName,
            definition = definition,
            // TODO
            classificationUri = "",
            // TODO
            unitTypes = emptyList(),
            // TODO
            subjectFields = emptyList(),
            containsUnitIdentifyingInformation = containsUnitIdentifyingInformation.toBoolean(),
            containsSensitivePersonalInformation = containsSensitivePersonalInformation.toBoolean(),
            variableStatus = variableStatus,
            measurementType = KlassReference("", "", ""),
            validFrom = validFrom,
            validUntil = validUntil,
            externalReferenceUri = externalReferenceUri,
            relatedVariableDefinitionUris = relatedVariableDefinitionUris,
            // TODO
            owner = null,
            contact = contact,
            // TODO
            createdAt = "",
            // TODO
            createdBy = null,
            // TODO
            lastUpdatedAt = "",
            // TODO
            lastUpdatedBy = null,
        )
}
