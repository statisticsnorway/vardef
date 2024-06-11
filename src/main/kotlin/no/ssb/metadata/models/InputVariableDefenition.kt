package no.ssb.metadata.models

import io.micronaut.core.annotation.Nullable
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import io.viascom.nanoid.NanoId
import jakarta.validation.constraints.Pattern
import no.ssb.metadata.constants.DEFINITION_FIELD_DESCRIPTION
import no.ssb.metadata.constants.NAME_FIELD_DESCRIPTION
import no.ssb.metadata.constants.SHORT_NAME_FIELD_DESCRIPTION

@MappedEntity
@Serdeable(naming = SnakeCaseStrategy::class)
@Schema(
    example = """
        {
            "name":
                {   "en": "English",
                    "nb": "Norwegian Bokmål",
                    "nn": "Norwegian Nynorsk"
                },
            "short_name": "string",
            "definition":
                {
                    "en": "English",
                    "nb": "Norwegian Bokmål",
                    "nn": "Norwegian Nynorsk"
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
    val classificationReference: String,
    val unitTypes: List<String>,
    val subjectFields: List<String>,
    val containsUnitIdentifyingInformation: Boolean,
    val containsSensitivePersonalInformation: Boolean,
    val variableStatus: String,
    val measurementType: String,
    val validFrom: String,
    val validUntil: String,
    val externalReferenceUri: String,
    val relatedVariableDefinitionUris: List<String>,
    val contact : Contact,
) {
    fun toSavedVariableDefinition(): SavedVariableDefinition =
        SavedVariableDefinition(
            definitionId = NanoId.generate(8),
            name = name,
            shortName = shortName,
            definition = definition,
            classificationUri = "",
            unitTypes = emptyList(),
            subjectFields = emptyList(),
            containsUnitIdentifyingInformation = containsUnitIdentifyingInformation,
            containsSensitivePersonalInformation = containsSensitivePersonalInformation,
            variableStatus = variableStatus,
            measurementType = KlassReference("", "", measurementType),
            validFrom = validFrom,
            validUntil = validUntil,
            externalReferenceUri = externalReferenceUri,
            relatedVariableDefinitionUris = relatedVariableDefinitionUris,
            owner = null,
            contact = contact,
            createdAt = "",
            createdBy = null,
            lastUpdatedAt = "",
            lastUpdatedBy = null,
        )
    }
